package ch.so.agi.gretl.steps.publisher.in.xtf.list;

import java.nio.file.Path;
import java.util.Objects;

import ch.so.agi.gretl.steps.publisher.operation.AbstractSingleInputSingleOutputParameters;

/**
 * Parameters for copying explicitly selected XTF or ITF files.
 */
public final class XtfCopyParams extends AbstractSingleInputSingleOutputParameters {
    public enum TransferFileType {
        XTF(".xtf"),
        ITF(".itf");

        private final String fileExtension;

        TransferFileType(String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getFileExtension() {
            return fileExtension;
        }
    }

    private final TransferFileType transferFileType;
    private final TransferFileList transferFileList;

    private XtfCopyParams(Path sourceDir, Path targetDir, TransferFileType transferFileType,
            TransferFileList transferFileList) {
        super(sourceDir, targetDir);
        this.transferFileType = Objects.requireNonNull(transferFileType, "transferFileType must not be null");
        this.transferFileList = Objects.requireNonNull(transferFileList, "transferFileList must not be null");
        if (transferFileList.size() == 0) {
            throw new IllegalArgumentException("transferFileList must not be empty");
        }
    }

    public static XtfCopyParams of(Path sourceDir, Path targetDir, TransferFileType transferFileType,
            TransferFileList transferFileList) {
        return new XtfCopyParams(sourceDir, targetDir, transferFileType, transferFileList);
    }

    public TransferFileType getTransferFileType() {
        return transferFileType;
    }

    public TransferFileList getTransferFileList() {
        return transferFileList;
    }

    public Path getSourceDir() {
        return getInputDir();
    }

    public Path getTargetDir() {
        return getOutputDir();
    }
}
