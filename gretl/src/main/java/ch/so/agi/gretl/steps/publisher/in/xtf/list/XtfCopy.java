package ch.so.agi.gretl.steps.publisher.in.xtf.list;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.util.copy.BatchFileCopier;

/**
 * Copies the requested transfer files to the target directory.
 */
public final class XtfCopy {
    private final BatchFileCopier batchFileCopier;

    public XtfCopy() {
        this(new BatchFileCopier());
    }

    XtfCopy(BatchFileCopier batchFileCopier) {
        this.batchFileCopier = Objects.requireNonNull(batchFileCopier, "batchFileCopier must not be null");
    }

    public List<Path> execute(Path sourceDir, Path targetDir, XtfCopyParams params) throws IOException {
        Objects.requireNonNull(sourceDir, "sourceDir must not be null");
        Objects.requireNonNull(targetDir, "targetDir must not be null");
        Objects.requireNonNull(params, "params must not be null");

        if (!Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException("sourceDir <" + sourceDir + "> must be an existing directory");
        }

        List<Path> sourceFiles = new ArrayList<Path>(params.getTransferFileList().size());
        for (String fileName : params.getTransferFileList()) {
            Path sourceFile = sourceDir.resolve(fileName + params.getTransferFileType().getFileExtension());
            if (!Files.exists(sourceFile)) {
                throw new java.nio.file.NoSuchFileException(sourceFile.toString());
            }
            sourceFiles.add(sourceFile);
        }

        return batchFileCopier.copyFiles(sourceFiles, targetDir);
    }
}
