package ch.so.agi.gretl.steps.publisher.in.xtf.list;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList; 
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.Operation;
import ch.so.agi.gretl.steps.publisher.util.BatchFileCopier;

/**
 * Copies the requested transfer files to the target directory.
 */
public final class XtfCopy implements Operation {
    private XtfCopyParams parameters;
    private final BatchFileCopier batchFileCopier;
    private List<Path> copiedFiles = List.of();

    public XtfCopy(XtfCopyParams parameters) {
        this(parameters, new BatchFileCopier());
    }

    @Deprecated public XtfCopy() { this.batchFileCopier = new BatchFileCopier(); }
    @Deprecated public void execute(XtfCopyParams parameters) throws IOException { this.parameters = parameters; execute(); }

    XtfCopy(XtfCopyParams parameters, BatchFileCopier batchFileCopier) {
        this.parameters = Objects.requireNonNull(parameters, "parameters must not be null");
        this.batchFileCopier = Objects.requireNonNull(batchFileCopier, "batchFileCopier must not be null");
    }

    @Override
    public void execute() throws IOException {
        copiedFiles = copyFiles(parameters);
    }

    @Override
    public String getHumanReadableName() { return "Source xtf/itf list copy"; }

    @Override
    public String getSuccessLogDetail() {
        return "copied " + copiedFiles.size() + " source file(s): " + copiedFiles;
    }

    List<Path> copyFiles(XtfCopyParams operationParameters) throws IOException {
        Objects.requireNonNull(operationParameters, "operationParameters must not be null");
        Path sourceDir = operationParameters.getSourceDir();
        Path targetDir = operationParameters.getTargetDir();

        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("sourceDir <" + sourceDir + "> must be an existing directory");
        }

        List<Path> sourceFiles = new ArrayList<Path>(operationParameters.getTransferFileList().size());
        for (String fileName : operationParameters.getTransferFileList()) {
            Path sourceFile = sourceDir.resolve(fileName + operationParameters.getTransferFileType().getFileExtension());
            if (!Files.exists(sourceFile)) {
                throw new java.nio.file.NoSuchFileException(sourceFile.toString());
            }
            sourceFiles.add(sourceFile);
        }

        return batchFileCopier.copyFiles(sourceFiles, targetDir);
    }
}
