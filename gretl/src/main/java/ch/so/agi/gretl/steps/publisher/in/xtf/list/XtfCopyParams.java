package ch.so.agi.gretl.steps.publisher.in.xtf.list;

import java.util.Objects;

/**
 * Parameters for copying explicitly selected XTF or ITF files.
 */
public final class XtfCopyParams {
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

    private XtfCopyParams(TransferFileType transferFileType, TransferFileList transferFileList) {
        this.transferFileType = Objects.requireNonNull(transferFileType, "transferFileType must not be null");
        this.transferFileList = Objects.requireNonNull(transferFileList, "transferFileList must not be null");
        if (transferFileList.size() == 0) {
            throw new IllegalArgumentException("transferFileList must not be empty");
        }
    }

    public static XtfCopyParams of(TransferFileType transferFileType, TransferFileList transferFileList) {
        return new XtfCopyParams(transferFileType, transferFileList);
    }

    public TransferFileType getTransferFileType() {
        return transferFileType;
    }

    public TransferFileList getTransferFileList() {
        return transferFileList;
    }
}
