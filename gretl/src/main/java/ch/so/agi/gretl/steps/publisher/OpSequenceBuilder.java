package ch.so.agi.gretl.steps.publisher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

    private final DbSelectionResolver dbSelectionResolver;

    public OpSequenceBuilder() {
        this(new DefaultDbSelectionResolver());
    }

    OpSequenceBuilder(DbSelectionResolver dbSelectionResolver) {
        this.dbSelectionResolver = Objects.requireNonNull(dbSelectionResolver, "dbSelectionResolver must not be null");
    }

    public List<OpSequenceStep> buildSequence(RawPublisherArgs rawPublisherArgs, Connection publicationDbConnection,
            Path cacheRoot) throws Exception {
        Objects.requireNonNull(rawPublisherArgs, "rawPublisherArgs must not be null");
        Objects.requireNonNull(publicationDbConnection, "publicationDbConnection must not be null");
        Path normalizedCacheRoot = normalizePath(cacheRoot, "cacheRoot");

        List<OpSequenceStep> steps = new ArrayList<>();
        SourcePlan sourcePlan = buildSourcePlan(rawPublisherArgs, publicationDbConnection, normalizedCacheRoot);
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

        steps.add(OpSequenceStep.of(new Packer(),
                constant(PackerParameters.of(normalizedCacheRoot, rawPublisherArgs.getOutDataIdent()))));
        steps.add(OpSequenceStep.of(new Writer(), new Supplier<WriterParameters>() {
            @Override
            public WriterParameters get() {
                return WriterParameters.of(publicationDbConnection, rawPublisherArgs.getOutDataIdent(),
                        discoverPackedArtifacts(normalizedCacheRoot));
            }
        }));
        steps.add(OpSequenceStep.of(new RemoteUpdater(),
                constant(RemoteUpdaterParameters.of(normalizedCacheRoot, endpointToPath(rawPublisherArgs.getOutBasePath()),
                        rawPublisherArgs.getOutDataIdent(), rawPublisherArgs.getDepVersion()))));

        return Collections.unmodifiableList(steps);
    }

    private SourcePlan buildSourcePlan(RawPublisherArgs rawPublisherArgs, Connection publicationDbConnection, Path cacheRoot)
            throws Exception {
        switch (rawPublisherArgs.getPublishMode()) {
        case dbIdentvaluesList:
        case dbIdentvaluesRegex:
            return buildDbSourcePlan(rawPublisherArgs, publicationDbConnection, cacheRoot);
        case xtfFilesList:
            return buildXtfListSourcePlan(rawPublisherArgs, cacheRoot);
        case xtfFilesRegex:
            return buildXtfRegexSourcePlan(rawPublisherArgs, cacheRoot);
        default:
            throw new IllegalArgumentException("unsupported publishMode <" + rawPublisherArgs.getPublishMode() + ">");
        }
    }

    private SourcePlan buildDbSourcePlan(RawPublisherArgs rawPublisherArgs, Connection publicationDbConnection,
            Path cacheRoot) throws Exception {
        DataSelection requestedSelection = createRequestedSelection(rawPublisherArgs);
        DataSelection resolvedSelection = dbSelectionResolver.resolve(publicationDbConnection, rawPublisherArgs.getDbSchema(),
                requestedSelection);
        List<String> keyValues = resolvedSelection.getKeyValues();

        if (Boolean.TRUE.equals(rawPublisherArgs.getDbMergeToSingleXtf()) || keyValues.size() <= 1) {
            return SourcePlan.singleStage(OpSequenceStep.of(new Exporter(),
                    constant(ExporterParameters.of(resolvedSelection, publicationDbConnection, rawPublisherArgs.getDbSchema(),
                            Boolean.TRUE.equals(rawPublisherArgs.getDbMergeToSingleXtf()), cacheRoot))));
        }

        List<OpSequenceStep> steps = new ArrayList<>();
        List<Path> stageDirs = new ArrayList<>();
        Path stageRoot = siblingStageRoot(cacheRoot);
        for (int i = 0; i < keyValues.size(); i++) {
            String keyValue = keyValues.get(i);
            Path stageDir = stageRoot.resolve(String.format(Locale.ROOT, "%02d-%s", Integer.valueOf(i),
                    sanitizeForPathSegment(keyValue)));
            DataSelection singleSelection = new DataSelection();
            singleSelection.setKeyType(resolvedSelection.getKeyType());
            singleSelection.setKeyValues(Collections.singletonList(keyValue));
            stageDirs.add(stageDir);
            steps.add(OpSequenceStep.of(new Exporter(), constant(
                    ExporterParameters.of(singleSelection, publicationDbConnection, rawPublisherArgs.getDbSchema(), false,
                            stageDir))));
        }
        return SourcePlan.multiStage(steps, stageDirs);
    }

    private SourcePlan buildXtfRegexSourcePlan(RawPublisherArgs rawPublisherArgs, Path cacheRoot) {
        Path sourceDir = coercePath(rawPublisherArgs.getXtfFile_FolderPath(), "xtfFile_FolderPath");
        return SourcePlan.singleStage(OpSequenceStep.of(new XtfByRegex(),
                constant(XtfByRegexParams.of(sourceDir, cacheRoot, rawPublisherArgs.getXtfFilename_Regex()))));
    }

    private SourcePlan buildXtfListSourcePlan(RawPublisherArgs rawPublisherArgs, Path cacheRoot) {
        Path sourceDir = coercePath(rawPublisherArgs.getXtfFile_FolderPath(), "xtfFile_FolderPath");
        ParsedTransferSelection parsedSelection = parseTransferSelection(rawPublisherArgs.getXtfFilename_List());
        if (parsedSelection.getBasenames().size() <= 1) {
            return SourcePlan.singleStage(OpSequenceStep.of(new XtfCopy(),
                    constant(XtfCopyParams.of(sourceDir, cacheRoot, parsedSelection.getTransferFileType(),
                            TransferFileList.of(parsedSelection.getBasenames())))));
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
        return SourcePlan.multiStage(steps, stageDirs);
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

        private SourcePlan(List<OpSequenceStep> steps, List<Path> stageDirs) {
            this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
            this.stageDirs = Collections.unmodifiableList(new ArrayList<>(stageDirs));
        }

        private static SourcePlan singleStage(OpSequenceStep step) {
            return new SourcePlan(Collections.singletonList(step), Collections.emptyList());
        }

        private static SourcePlan multiStage(List<OpSequenceStep> steps, List<Path> stageDirs) {
            return new SourcePlan(steps, stageDirs);
        }

        private List<OpSequenceStep> getSteps() {
            return steps;
        }

        private List<Path> getStageDirs() {
            return stageDirs;
        }

        private boolean requiresMerge() {
            return !stageDirs.isEmpty();
        }
    }
}
