package ch.so.agi.gretl.steps.publisher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.so.agi.gretl.api.Endpoint;
import ch.so.agi.gretl.steps.publisher.in.db.DataSelection;
import ch.so.agi.gretl.steps.publisher.in.db.Exporter;
import ch.so.agi.gretl.steps.publisher.in.db.ExporterParameters;
import ch.so.agi.gretl.steps.publisher.in.db.SelectionMapper;
import ch.so.agi.gretl.steps.publisher.in.xtf.list.TransferFileList;
import ch.so.agi.gretl.steps.publisher.in.xtf.list.XtfCopy;
import ch.so.agi.gretl.steps.publisher.in.xtf.list.XtfCopyParams;
import ch.so.agi.gretl.steps.publisher.in.xtf.regex.XtfByRegex;
import ch.so.agi.gretl.steps.publisher.in.xtf.regex.XtfByRegexParams;
import ch.so.agi.gretl.steps.publisher.operation.OperationParameters;
import ch.so.agi.gretl.steps.publisher.out.metainfo.table.Writer;
import ch.so.agi.gretl.steps.publisher.out.metainfo.table.WriterParameters;
import ch.so.agi.gretl.steps.publisher.out.metainfo.metafolder.MetafolderWriter;
import ch.so.agi.gretl.steps.publisher.out.metainfo.metafolder.MetafolderWriterParameters;
import ch.so.agi.gretl.steps.publisher.out.updateremote.RemoteUpdater;
import ch.so.agi.gretl.steps.publisher.out.updateremote.RemoteUpdaterParameters;
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.Derivator;
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivatorParameters;
import ch.so.agi.gretl.steps.publisher.stage.mergestages.MergeStages;
import ch.so.agi.gretl.steps.publisher.stage.mergestages.MergeStagesParameters;
import ch.so.agi.gretl.steps.publisher.stage.pack.Packer;
import ch.so.agi.gretl.steps.publisher.stage.pack.PackerParameters;
import ch.so.agi.gretl.steps.publisher.stage.validation.CacheValidator;
import ch.so.agi.gretl.steps.publisher.stage.validation.CacheValidatorParameters;
import ch.so.agi.gretl.steps.publisher.stage.validation.ValidationConfigSeeder;
import ch.so.agi.gretl.steps.publisher.stage.validation.ValidationConfigSeederParameters;

/**
 * Builds the sequence of publisher operations from raw publisher arguments.
 */
public class OpSequenceBuilder {
    private static final String STAGE_ROOT_SUFFIX = ".source-stages";
    private static final String ALL_PARTS = "allparts";
    private static final ZoneId PUBLICATION_ZONE = ZoneId.of("Europe/Zurich");

    private final DbSelectionResolver dbSelectionResolver;

    public OpSequenceBuilder() {
        this(new DefaultDbSelectionResolver());
    }

    OpSequenceBuilder(DbSelectionResolver dbSelectionResolver) {
        this.dbSelectionResolver = Objects.requireNonNull(dbSelectionResolver, "dbSelectionResolver must not be null");
    }

    public List<OpSequenceStep> buildSequence(RawPublisherArgs rawPublisherArgs, Connection publicationDbConnection,
            Path cacheRoot) throws Exception {
        return buildSequence(rawPublisherArgs, publicationDbConnection, publicationDbConnection, "public", true,
                cacheRoot);
    }

    /** Builds a sequence with separate source and publication-metadata connections. */
    public List<OpSequenceStep> buildSequence(RawPublisherArgs rawPublisherArgs, Connection sourceDbConnection,
            Connection publicationDbConnection, Path cacheRoot) throws Exception {
        return buildSequence(rawPublisherArgs, sourceDbConnection, publicationDbConnection, "public", true, cacheRoot);
    }

    /** Builds a sequence that optionally writes metadata after remote promotion. */
    public List<OpSequenceStep> buildSequence(RawPublisherArgs rawPublisherArgs, Connection sourceDbConnection,
            Connection publicationDbConnection, String metadataSchema, boolean writeMetadata, Path cacheRoot) throws Exception {
        return buildSequence(rawPublisherArgs, sourceDbConnection, publicationDbConnection, metadataSchema, writeMetadata,
                null, null, null, cacheRoot);
    }

    /** Builds a sequence with optional JSON metadata source configuration. */
    public List<OpSequenceStep> buildSequence(RawPublisherArgs rawPublisherArgs, Connection sourceDbConnection,
            Connection publicationDbConnection, String metadataSchema, boolean writeMetadata, String jsonmetaAddress,
            String jsonmetaBucket, String jsonmetaFileName, Path cacheRoot) throws Exception {
        Objects.requireNonNull(rawPublisherArgs, "rawPublisherArgs must not be null");
        if (writeMetadata) {
            Objects.requireNonNull(publicationDbConnection, "publicationDbConnection must not be null when writing metadata");
            requireText(metadataSchema, "metadataSchema");
        }
        Path normalizedCacheRoot = normalizePath(cacheRoot, "cacheRoot");

        List<OpSequenceStep> steps = new ArrayList<>();
        SourcePlan sourcePlan = buildSourcePlan(rawPublisherArgs, sourceDbConnection, normalizedCacheRoot);
        steps.addAll(sourcePlan.getSteps());

        if (sourcePlan.requiresMerge()) {
            steps.add(OpSequenceStep.of(new MergeStages(),
                    constant(MergeStagesParameters.of(sourcePlan.getStageDirs(), normalizedCacheRoot))));
        }

        Path validationConfig = coerceOptionalPath(rawPublisherArgs.getOutValidationConfigFilePath(),
                "outValidationConfigFilePath");
        if (validationConfig != null) {
            steps.add(OpSequenceStep.of(new ValidationConfigSeeder(),
                    constant(ValidationConfigSeederParameters.of(validationConfig, normalizedCacheRoot))));
            steps.add(OpSequenceStep.of(new CacheValidator(),
                    constant(CacheValidatorParameters.of(normalizedCacheRoot, true, true))));
        }

        if (!rawPublisherArgs.getOutDerivedFormats().isEmpty()) {
            steps.add(OpSequenceStep.of(new Derivator(),
                    constant(DerivatorParameters.of(normalizedCacheRoot, rawPublisherArgs.getOutDerivedFormats()))));
        }

        steps.add(OpSequenceStep.of(new MetafolderWriter(), constant(MetafolderWriterParameters.of(normalizedCacheRoot,
                rawPublisherArgs.getOutDataIdent(), rawPublisherArgs.getCustomModelDir(), validationConfig,
                jsonmetaAddress, jsonmetaBucket, jsonmetaFileName))));

        steps.add(OpSequenceStep.of(new Packer(),
                constant(PackerParameters.of(normalizedCacheRoot, rawPublisherArgs.getOutDataIdent()))));
        steps.add(OpSequenceStep.of(new RemoteUpdater(),
                constant(RemoteUpdaterParameters.of(normalizedCacheRoot, endpointToPath(rawPublisherArgs.getOutBasePath()),
                        rawPublisherArgs.getOutDataIdent(), rawPublisherArgs.getDepVersion()))));
        if (writeMetadata) {
            steps.add(OpSequenceStep.of(new Writer(), new Supplier<WriterParameters>() {
                @Override
                public WriterParameters get() {
                    return WriterParameters.of(publicationDbConnection, metadataSchema, rawPublisherArgs.getOutDataIdent(),
                            publicationDate(rawPublisherArgs), sourcePlan.getPartIdentifiers(),
                            discoverPublishedFormats(normalizedCacheRoot), rawPublisherArgs.getOutDerivedFormats());
                }
            }));
        }

        return Collections.unmodifiableList(steps);
    }

    private SourcePlan buildSourcePlan(RawPublisherArgs rawPublisherArgs, Connection sourceDbConnection, Path cacheRoot)
            throws Exception {
        switch (rawPublisherArgs.getPublishMode()) {
        case dbIdentvaluesList:
        case dbIdentvaluesRegex:
            return buildDbSourcePlan(rawPublisherArgs, sourceDbConnection, cacheRoot);
        case xtfFilesList:
            return buildXtfListSourcePlan(rawPublisherArgs, cacheRoot);
        case xtfFilesRegex:
            return buildXtfRegexSourcePlan(rawPublisherArgs, cacheRoot);
        default:
            throw new IllegalArgumentException("unsupported publishMode <" + rawPublisherArgs.getPublishMode() + ">");
        }
    }

    private SourcePlan buildDbSourcePlan(RawPublisherArgs rawPublisherArgs, Connection sourceDbConnection,
            Path cacheRoot) throws Exception {
        Objects.requireNonNull(sourceDbConnection, "sourceDbConnection must not be null in db mode");
        DataSelection requestedSelection = createRequestedSelection(rawPublisherArgs);
        DataSelection resolvedSelection = dbSelectionResolver.resolve(sourceDbConnection, rawPublisherArgs.getDbSchema(),
                requestedSelection);
        List<String> keyValues = resolvedSelection.getKeyValues();
        List<String> partIdentifiers = rawPublisherArgs.getDbIliIdent_Values().size() == 1
                ? Collections.singletonList(ALL_PARTS)
                : new ArrayList<String>(keyValues);

        if (Boolean.TRUE.equals(rawPublisherArgs.getDbMergeToSingleXtf()) || keyValues.size() <= 1) {
            return SourcePlan.singleStage(OpSequenceStep.of(new Exporter(),
                    constant(ExporterParameters.of(resolvedSelection, sourceDbConnection, rawPublisherArgs.getDbSchema(),
                            Boolean.TRUE.equals(rawPublisherArgs.getDbMergeToSingleXtf()), cacheRoot))), partIdentifiers);
        }

        List<OpSequenceStep> steps = new ArrayList<>();
        List<Path> stageDirs = new ArrayList<>();
        Path stageRoot = siblingStageRoot(cacheRoot);
        for (int i = 0; i < keyValues.size(); i++) {
            String keyValue = keyValues.get(i);
            Path stageDir = stageRoot.resolve(String.format(Locale.ROOT, "%02d-%s", Integer.valueOf(i),
                    sanitizeForPathSegment(keyValue)));
            Files.createDirectories(stageDir);
            DataSelection singleSelection = new DataSelection();
            singleSelection.setKeyType(resolvedSelection.getKeyType());
            singleSelection.setKeyValues(Collections.singletonList(keyValue));
            stageDirs.add(stageDir);
            steps.add(OpSequenceStep.of(new Exporter(), constant(
                    ExporterParameters.of(singleSelection, sourceDbConnection, rawPublisherArgs.getDbSchema(), false,
                            stageDir))));
        }
        return SourcePlan.multiStage(steps, stageDirs, partIdentifiers);
    }

    private SourcePlan buildXtfRegexSourcePlan(RawPublisherArgs rawPublisherArgs, Path cacheRoot) {
        Path sourceDir = coercePath(rawPublisherArgs.getXtfFile_FolderPath(), "xtfFile_FolderPath");
        List<String> partIdentifiers = discoverRegexPartIdentifiers(sourceDir, rawPublisherArgs.getXtfFilename_Regex());
        return SourcePlan.singleStage(OpSequenceStep.of(new XtfByRegex(),
                constant(XtfByRegexParams.of(sourceDir, cacheRoot, rawPublisherArgs.getXtfFilename_Regex()))), partIdentifiers);
    }

    private SourcePlan buildXtfListSourcePlan(RawPublisherArgs rawPublisherArgs, Path cacheRoot) {
        Path sourceDir = coercePath(rawPublisherArgs.getXtfFile_FolderPath(), "xtfFile_FolderPath");
        ParsedTransferSelection parsedSelection = parseTransferSelection(rawPublisherArgs.getXtfFilename_List());
        List<String> partIdentifiers = parsedSelection.getBasenames().size() == 1
                ? Collections.singletonList(ALL_PARTS)
                : parsedSelection.getBasenames();
        if (parsedSelection.getBasenames().size() <= 1) {
            return SourcePlan.singleStage(OpSequenceStep.of(new XtfCopy(),
                    constant(XtfCopyParams.of(sourceDir, cacheRoot, parsedSelection.getTransferFileType(),
                            TransferFileList.of(parsedSelection.getBasenames())))), partIdentifiers);
        }

        List<OpSequenceStep> steps = new ArrayList<>();
        List<Path> stageDirs = new ArrayList<>();
        Path stageRoot = siblingStageRoot(cacheRoot);
        for (int i = 0; i < parsedSelection.getBasenames().size(); i++) {
            String basename = parsedSelection.getBasenames().get(i);
            Path stageDir = stageRoot.resolve(String.format(Locale.ROOT, "%02d-%s", Integer.valueOf(i),
                    sanitizeForPathSegment(basename)));
            stageDirs.add(stageDir);
            steps.add(OpSequenceStep.of(new XtfCopy(),
                    constant(XtfCopyParams.of(sourceDir, stageDir, parsedSelection.getTransferFileType(),
                            TransferFileList.of(Collections.singletonList(basename))))));
        }
        return SourcePlan.multiStage(steps, stageDirs, partIdentifiers);
    }

    private DataSelection createRequestedSelection(RawPublisherArgs rawPublisherArgs) {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.valueOf(rawPublisherArgs.getDbIliIdentType().name()));
        if (!rawPublisherArgs.getDbIliIdent_Values().isEmpty()) {
            selection.setKeyValues(rawPublisherArgs.getDbIliIdent_Values());
        }
        if (rawPublisherArgs.getDbIliIdent_RegEx() != null) {
            selection.setKeyRegEx(rawPublisherArgs.getDbIliIdent_RegEx());
        }
        return selection;
    }

    private ParsedTransferSelection parseTransferSelection(List<String> configuredValues) {
        List<String> basenames = new ArrayList<>();
        XtfCopyParams.TransferFileType inferredType = null;

        for (String configuredValue : configuredValues) {
            String trimmed = requireText(configuredValue, "xtfFilename_List value");
            String lower = trimmed.toLowerCase(Locale.ROOT);
            XtfCopyParams.TransferFileType currentType = null;
            String basename = trimmed;

            if (lower.endsWith(".xtf")) {
                currentType = XtfCopyParams.TransferFileType.XTF;
                basename = trimmed.substring(0, trimmed.length() - 4);
            } else if (lower.endsWith(".itf")) {
                currentType = XtfCopyParams.TransferFileType.ITF;
                basename = trimmed.substring(0, trimmed.length() - 4);
            }

            if (currentType != null) {
                if (inferredType == null) {
                    inferredType = currentType;
                } else if (inferredType != currentType) {
                    throw new IllegalArgumentException("xtfFilename_List must not mix .xtf and .itf files");
                }
            }
            basenames.add(basename);
        }

        if (inferredType == null) {
            inferredType = XtfCopyParams.TransferFileType.XTF;
        }
        return new ParsedTransferSelection(TransferFileList.of(basenames).asList(), inferredType);
    }

    private static List<Path> discoverPackedArtifacts(Path cacheRoot) {
        if (!Files.isDirectory(cacheRoot)) {
            throw new IllegalArgumentException("cacheRoot must be an existing directory when resolving writer inputs: "
                    + cacheRoot);
        }
        try (Stream<Path> stream = Files.list(cacheRoot)) {
            List<Path> artifacts = stream.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip"))
                    .sorted()
                    .collect(Collectors.toList());
            if (artifacts.isEmpty()) {
                throw new IllegalArgumentException("no packed publication artifacts found in " + cacheRoot);
            }
            return artifacts;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("failed to inspect cacheRoot " + cacheRoot, e);
        }
    }

    private static List<String> discoverPublishedFormats(Path cacheRoot) {
        List<String> formats = new ArrayList<String>();
        for (Path artifact : discoverPackedArtifacts(cacheRoot)) {
            String filename = artifact.getFileName().toString().toLowerCase(Locale.ROOT);
            String base = filename.substring(0, filename.length() - 4);
            if ("geobau_dxf".equals(base)) {
                addDistinct(formats, "dxf_geobau");
            } else if ("gpkg".equals(base) || "shp".equals(base) || "dxf".equals(base)) {
                addDistinct(formats, base);
            } else {
                int extension = base.lastIndexOf('.');
                if (extension >= 0) {
                    String format = base.substring(extension + 1);
                    if ("xtf".equals(format) || "itf".equals(format)) {
                        addDistinct(formats, format);
                    }
                }
            }
        }
        if (formats.isEmpty()) {
            throw new IllegalArgumentException("no supported publication formats found in " + cacheRoot);
        }
        return formats;
    }

    private static void addDistinct(List<String> values, String value) {
        if (!values.contains(value)) {
            values.add(value);
        }
    }

    private static LocalDate publicationDate(RawPublisherArgs rawPublisherArgs) {
        return rawPublisherArgs.getDepVersion().toInstant().atZone(PUBLICATION_ZONE).toLocalDate();
    }

    private static List<String> discoverRegexPartIdentifiers(Path sourceDir, String regex) {
        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("sourceDir <" + sourceDir + "> must be an existing directory");
        }
        Pattern pattern = Pattern.compile(regex);
        try (Stream<Path> stream = Files.list(sourceDir)) {
            List<String> identifiers = stream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(filename -> pattern.matcher(filename).matches())
                    .sorted()
                    .map(OpSequenceBuilder::transferBasename)
                    .collect(Collectors.toList());
            if (identifiers.isEmpty()) {
                throw new IllegalArgumentException("regex <" + regex + "> did not match any files");
            }
            return identifiers;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("failed to inspect sourceDir " + sourceDir, e);
        }
    }

    private static String transferBasename(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".xtf") || lower.endsWith(".itf")) {
            return filename.substring(0, filename.length() - 4);
        }
        return filename;
    }

    private static Path endpointToPath(Endpoint endpoint) {
        Objects.requireNonNull(endpoint, "outBasePath must not be null");
        return normalizePath(Path.of(requireText(endpoint.getUrl(), "outBasePath.url")), "outBasePath");
    }

    private static Path siblingStageRoot(Path cacheRoot) {
        Path parent = cacheRoot.getParent();
        String fileName = cacheRoot.getFileName() != null ? cacheRoot.getFileName().toString() : "cache";
        if (parent == null) {
            return Path.of(fileName + STAGE_ROOT_SUFFIX).toAbsolutePath().normalize();
        }
        return parent.resolve(fileName + STAGE_ROOT_SUFFIX).toAbsolutePath().normalize();
    }

    private static Path coerceOptionalPath(String value, String fieldName) {
        return value == null ? null : coercePath(value, fieldName);
    }

    private static Path coercePath(String value, String fieldName) {
        return normalizePath(Path.of(requireText(value, fieldName)), fieldName);
    }

    private static Path normalizePath(Path path, String fieldName) {
        Objects.requireNonNull(path, fieldName + " must not be null");
        return path.toAbsolutePath().normalize();
    }

    private static String sanitizeForPathSegment(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private static <P extends OperationParameters> Supplier<P> constant(P parameters) {
        return new Supplier<P>() {
            @Override
            public P get() {
                return parameters;
            }
        };
    }

    interface DbSelectionResolver {
        DataSelection resolve(Connection publicationDbConnection, String dbSchema, DataSelection requestedSelection)
                throws Exception;
    }

    private static final class DefaultDbSelectionResolver implements DbSelectionResolver {
        @Override
        public DataSelection resolve(Connection publicationDbConnection, String dbSchema, DataSelection requestedSelection)
                throws Exception {
            return new SelectionMapper(requestedSelection, publicationDbConnection, dbSchema).map();
        }
    }

    private static final class ParsedTransferSelection {
        private final List<String> basenames;
        private final XtfCopyParams.TransferFileType transferFileType;

        private ParsedTransferSelection(List<String> basenames, XtfCopyParams.TransferFileType transferFileType) {
            this.basenames = basenames;
            this.transferFileType = transferFileType;
        }

        private List<String> getBasenames() {
            return basenames;
        }

        private XtfCopyParams.TransferFileType getTransferFileType() {
            return transferFileType;
        }
    }

    private static final class SourcePlan {
        private final List<OpSequenceStep> steps;
        private final List<Path> stageDirs;
        private final List<String> partIdentifiers;

        private SourcePlan(List<OpSequenceStep> steps, List<Path> stageDirs, List<String> partIdentifiers) {
            this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
            this.stageDirs = Collections.unmodifiableList(new ArrayList<>(stageDirs));
            this.partIdentifiers = Collections.unmodifiableList(new ArrayList<>(partIdentifiers));
        }

        private static SourcePlan singleStage(OpSequenceStep step, List<String> partIdentifiers) {
            return new SourcePlan(Collections.singletonList(step), Collections.emptyList(), partIdentifiers);
        }

        private static SourcePlan multiStage(List<OpSequenceStep> steps, List<Path> stageDirs,
                List<String> partIdentifiers) {
            return new SourcePlan(steps, stageDirs, partIdentifiers);
        }

        private List<OpSequenceStep> getSteps() {
            return steps;
        }

        private List<Path> getStageDirs() {
            return stageDirs;
        }

        private List<String> getPartIdentifiers() {
            return partIdentifiers;
        }

        private boolean requiresMerge() {
            return !stageDirs.isEmpty();
        }
    }
}
