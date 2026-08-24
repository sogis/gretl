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
import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.out.metainfo.table.MetaTableWriter;
import ch.so.agi.gretl.steps.publisher.out.metainfo.table.MetaTableWriterParameters;
import ch.so.agi.gretl.steps.publisher.out.metainfo.metafolder.MetaFolderWriter;
import ch.so.agi.gretl.steps.publisher.out.metainfo.metafolder.MetaFolderWriterParameters;
import ch.so.agi.gretl.steps.publisher.out.updateremote.RemoteUpdater;
import ch.so.agi.gretl.steps.publisher.out.updateremote.RemoteUpdaterParameters;
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.Derivator;
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivatorParameters;
import ch.so.agi.gretl.steps.publisher.stage.derivedformats.DerivedFormat;
import ch.so.agi.gretl.steps.publisher.stage.pack.PublicationArtifactPackager;
import ch.so.agi.gretl.steps.publisher.stage.pack.PublicationArtifactPackagerParameters;
import ch.so.agi.gretl.steps.publisher.stage.pack.OutputFormat;
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

    /** Builds the executable sequence from resolved arguments and opened connections. */
    public List<Operation> buildSequence(ResolvedPublisherArgs publisherArgs, Connection sourceDbConnection,
            Connection publicationDbConnection, Path cacheRoot) throws Exception {
        Objects.requireNonNull(publisherArgs, "publisherArgs must not be null");
        boolean writeMetadata = publisherArgs.getOutWriteMetadata();
        if (writeMetadata) {
            Objects.requireNonNull(publicationDbConnection, "publicationDbConnection must not be null when writing metadata");
            requireText(publisherArgs.getMetadataSchema(), "metadataSchema");
        }
        Path normalizedCacheRoot = normalizePath(cacheRoot, "cacheRoot");

        List<Operation> steps = new ArrayList<>();
        Path rawRoot = PartWorkspace.rawRoot(normalizedCacheRoot);
        Path publicationRoot = PartWorkspace.publicationRoot(normalizedCacheRoot);
        SourcePlan sourcePlan = buildSourcePlan(publisherArgs, sourceDbConnection, rawRoot);
        steps.addAll(sourcePlan.getSteps());

        Path validationConfig = coerceOptionalPath(publisherArgs.getOutValidationConfigFilePath(),
                "outValidationConfigFilePath");
        if (validationConfig != null) {
            steps.add(new ValidationConfigSeeder(ValidationConfigSeederParameters.of(validationConfig, rawRoot)));
        }
        steps.add(new CacheValidator(CacheValidatorParameters.of(rawRoot, true, true, publisherArgs.getCustomModelDir())));

        List<DerivedFormat> derivedFormats = selectedDerivedFormats(publisherArgs.getOutFormats());
        if (!derivedFormats.isEmpty()) {
            steps.add(new Derivator(DerivatorParameters.of(rawRoot, derivedFormats, publisherArgs.getCustomModelDir())));
        }

        steps.add(new PublicationArtifactPackager(PublicationArtifactPackagerParameters.of(rawRoot, publicationRoot,
                publisherArgs.getOutFormats())));
        steps.add(new MetaFolderWriter(MetaFolderWriterParameters.of(rawRoot, publicationRoot,
                publisherArgs.getOutDataIdent(), publisherArgs.getCustomModelDir(), validationConfig,
                publisherArgs.getJsonmetaAddress(), publisherArgs.getJsonmetaBucket(), publisherArgs.getJsonmetaFileName())));
        if (writeMetadata) {
            steps.add(new MetaTableWriter(MetaTableWriterParameters.of(publicationDbConnection, publisherArgs.getMetadataSchema(),
                    publisherArgs.getOutDataIdent(), publicationDate(publisherArgs), rawRoot, publicationRoot)));
        }
        steps.add(new RemoteUpdater(RemoteUpdaterParameters.of(publicationRoot,
                endpointToPath(publisherArgs.getOutFolderPath()), publisherArgs.getOutDataIdent(), publisherArgs.getPublicationTimestamp())));

        return Collections.unmodifiableList(steps);
    }

    private List<DerivedFormat> selectedDerivedFormats(List<OutputFormat> outputFormats) {
        List<DerivedFormat> formats = new ArrayList<DerivedFormat>();
        for (OutputFormat outputFormat : outputFormats) {
            DerivedFormat derivedFormat = outputFormat.getDerivedFormat();
            if (derivedFormat != null) {
                formats.add(derivedFormat);
            }
        }
        return formats;
    }

    private SourcePlan buildSourcePlan(ResolvedPublisherArgs publisherArgs, Connection sourceDbConnection, Path cacheRoot)
            throws Exception {
        switch (publisherArgs.getPublishMode()) {
        case dbIdentvaluesList:
        case dbIdentvaluesRegex:
            return buildDbSourcePlan(publisherArgs, sourceDbConnection, cacheRoot);
        case xtfFilesList:
            return buildXtfListSourcePlan(publisherArgs, cacheRoot);
        case xtfFilesRegex:
            return buildXtfRegexSourcePlan(publisherArgs, cacheRoot);
        default:
            throw new IllegalArgumentException("unsupported publishMode <" + publisherArgs.getPublishMode() + ">");
        }
    }

    private SourcePlan buildDbSourcePlan(ResolvedPublisherArgs publisherArgs, Connection sourceDbConnection,
            Path cacheRoot) throws Exception {
        Objects.requireNonNull(sourceDbConnection, "sourceDbConnection must not be null in db mode");
        DataSelection requestedSelection = createRequestedSelection(publisherArgs);
        DataSelection resolvedSelection = dbSelectionResolver.resolve(sourceDbConnection, publisherArgs.getDbSchema(),
                requestedSelection);
        List<String> keyValues = resolvedSelection.getKeyValues();
        List<String> partIdentifiers = publisherArgs.getDbIliIdent_Values().size() == 1
                ? Collections.singletonList(ALL_PARTS)
                : new ArrayList<String>(keyValues);

        if (Boolean.TRUE.equals(publisherArgs.getDbMergeToSingleXtf()) || keyValues.size() <= 1) {
            return SourcePlan.singleStage(new Exporter(ExporterParameters.of(resolvedSelection, sourceDbConnection,
                    publisherArgs.getDbSchema(), Boolean.TRUE.equals(publisherArgs.getDbMergeToSingleXtf()),
                    PartWorkspace.transferRoot(cacheRoot, partIdentifiers.get(0)),
                    PartWorkspace.workRoot(cacheRoot.getParent()))), partIdentifiers);
        }

        List<Operation> steps = new ArrayList<>();
        for (int i = 0; i < keyValues.size(); i++) {
            String keyValue = keyValues.get(i);
            DataSelection singleSelection = new DataSelection();
            singleSelection.setKeyType(resolvedSelection.getKeyType());
            singleSelection.setKeyValues(Collections.singletonList(keyValue));
            steps.add(new Exporter(ExporterParameters.of(singleSelection, sourceDbConnection, publisherArgs.getDbSchema(), false,
                    PartWorkspace.transferRoot(cacheRoot, keyValue), PartWorkspace.workRoot(cacheRoot.getParent()))));
        }
        return SourcePlan.of(steps, partIdentifiers);
    }

    private SourcePlan buildXtfRegexSourcePlan(ResolvedPublisherArgs publisherArgs, Path cacheRoot) {
        Path sourceDir = coercePath(publisherArgs.getXtfFile_FolderPath(), "xtfFile_FolderPath");
        List<RegexTransferFile> matchingFiles = discoverRegexTransferFiles(sourceDir,
                publisherArgs.getXtfFilename_Regex());
        List<Operation> steps = new ArrayList<>();
        List<String> partIdentifiers = new ArrayList<>();
        for (RegexTransferFile matchingFile : matchingFiles) {
            partIdentifiers.add(matchingFile.getPartIdentifier());
            steps.add(new XtfByRegex(XtfByRegexParams.of(sourceDir,
                    PartWorkspace.transferRoot(cacheRoot, matchingFile.getPartIdentifier()),
                    Pattern.quote(matchingFile.getFilename()))));
        }
        return SourcePlan.of(steps, partIdentifiers);
    }

    private SourcePlan buildXtfListSourcePlan(ResolvedPublisherArgs publisherArgs, Path cacheRoot) {
        Path sourceDir = coercePath(publisherArgs.getXtfFile_FolderPath(), "xtfFile_FolderPath");
        ParsedTransferSelection parsedSelection = parseTransferSelection(publisherArgs.getXtfFilename_List());
        List<String> partIdentifiers = parsedSelection.getBasenames().size() == 1
                ? Collections.singletonList(ALL_PARTS)
                : parsedSelection.getBasenames();
        if (parsedSelection.getBasenames().size() <= 1) {
            String partIdentifier = partIdentifiers.get(0);
            return SourcePlan.singleStage(new XtfCopy(XtfCopyParams.of(sourceDir,
                    PartWorkspace.transferRoot(cacheRoot, partIdentifier), parsedSelection.getTransferFileType(),
                    TransferFileList.of(parsedSelection.getBasenames()))), partIdentifiers);
        }

        List<Operation> steps = new ArrayList<>();
        for (int i = 0; i < parsedSelection.getBasenames().size(); i++) {
            String basename = parsedSelection.getBasenames().get(i);
            steps.add(new XtfCopy(XtfCopyParams.of(sourceDir, PartWorkspace.transferRoot(cacheRoot, basename), parsedSelection.getTransferFileType(),
                    TransferFileList.of(Collections.singletonList(basename)))));
        }
        return SourcePlan.of(steps, partIdentifiers);
    }

    private DataSelection createRequestedSelection(ResolvedPublisherArgs publisherArgs) {
        DataSelection selection = new DataSelection();
        selection.setKeyType(DataSelection.KeyType.valueOf(publisherArgs.getDbIliIdentType().name()));
        if (!publisherArgs.getDbIliIdent_Values().isEmpty()) {
            selection.setKeyValues(publisherArgs.getDbIliIdent_Values());
        }
        if (publisherArgs.getDbIliIdent_RegEx() != null) {
            selection.setKeyRegEx(publisherArgs.getDbIliIdent_RegEx());
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

    private static LocalDate publicationDate(ResolvedPublisherArgs publisherArgs) {
        return publisherArgs.getPublicationTimestamp().toInstant().atZone(PUBLICATION_ZONE).toLocalDate();
    }

    private static List<RegexTransferFile> discoverRegexTransferFiles(Path sourceDir, String regex) {
        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("sourceDir <" + sourceDir + "> must be an existing directory");
        }
        Pattern pattern = Pattern.compile(regex);
        try (Stream<Path> stream = Files.list(sourceDir)) {
            List<String> filenames = stream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(filename -> pattern.matcher(filename).matches())
                    .sorted()
                    .collect(Collectors.toList());
            if (filenames.isEmpty()) {
                throw new IllegalArgumentException("regex <" + regex + "> did not match any files");
            }
            List<RegexTransferFile> transferFiles = new ArrayList<>();
            for (String filename : filenames) {
                if (!isTransferFilename(filename)) {
                    throw new IllegalArgumentException("regex <" + regex + "> matched non-transfer file <" + filename
                            + ">; Publisher input files must use the .xtf or .itf extension");
                }
                String partIdentifier = transferBasename(filename);
                if (transferFiles.stream().anyMatch(file -> file.getPartIdentifier().equals(partIdentifier))) {
                    throw new IllegalArgumentException("regex <" + regex + "> matched multiple transfer files for publication part <"
                            + partIdentifier + ">");
                }
                transferFiles.add(new RegexTransferFile(filename, partIdentifier));
            }
            return transferFiles;
        } catch (java.io.IOException e) {
            throw new IllegalStateException("failed to inspect sourceDir " + sourceDir, e);
        }
    }

    private static boolean isTransferFilename(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".xtf") || lower.endsWith(".itf");
    }

    private static String transferBasename(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".xtf") || lower.endsWith(".itf")) {
            return filename.substring(0, filename.length() - 4);
        }
        return filename;
    }

    private static Path endpointToPath(Endpoint endpoint) {
        Objects.requireNonNull(endpoint, "outFolderPath must not be null");
        return normalizePath(Path.of(requireText(endpoint.getUrl(), "outFolderPath.url")), "outFolderPath");
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

    private static final class RegexTransferFile {
        private final String filename;
        private final String partIdentifier;

        private RegexTransferFile(String filename, String partIdentifier) {
            this.filename = filename;
            this.partIdentifier = partIdentifier;
        }

        private String getFilename() {
            return filename;
        }

        private String getPartIdentifier() {
            return partIdentifier;
        }
    }

    private static final class SourcePlan {
        private final List<Operation> steps;
        private final List<String> partIdentifiers;

        private SourcePlan(List<Operation> steps, List<String> partIdentifiers) {
            this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
            this.partIdentifiers = Collections.unmodifiableList(new ArrayList<>(partIdentifiers));
        }

        private static SourcePlan singleStage(Operation step, List<String> partIdentifiers) {
            return new SourcePlan(Collections.singletonList(step), partIdentifiers);
        }

        private static SourcePlan of(List<Operation> steps, List<String> partIdentifiers) {
            if (steps.isEmpty()) throw new IllegalArgumentException("source plan requires operations");
            return new SourcePlan(steps, partIdentifiers);
        }

        private List<Operation> getSteps() {
            return steps;
        }

        private List<String> getPartIdentifiers() {
            return partIdentifiers;
        }
    }
}
