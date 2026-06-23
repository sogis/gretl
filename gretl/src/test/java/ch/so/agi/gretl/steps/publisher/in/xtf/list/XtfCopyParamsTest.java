package ch.so.agi.gretl.steps.publisher.in.xtf.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class XtfCopyParamsTest {
    @Test
    void storesExplicitTransferFileTypeAndList() {
        TransferFileList fileList = TransferFileList.of("alpha");

        XtfCopyParams params = XtfCopyParams.of(XtfCopyParams.TransferFileType.XTF, fileList);

        assertEquals(XtfCopyParams.TransferFileType.XTF, params.getTransferFileType());
        assertEquals(fileList, params.getTransferFileList());
    }

    @Test
    void rejectsNullTransferFileType() {
        TransferFileList fileList = TransferFileList.of("alpha");

        assertThrows(NullPointerException.class, () -> XtfCopyParams.of(null, fileList));
    }

    @Test
    void rejectsNullTransferFileList() {
        assertThrows(NullPointerException.class, () -> XtfCopyParams.of(XtfCopyParams.TransferFileType.ITF, null));
    }
}
