package ch.so.agi.gretl.steps.publisher.in.xtf.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class XtfCopyParamsTest {
    @Test
    void storesExplicitTransferFileTypeAndList() {
        TransferFileList fileList = TransferFileList.of("alpha");
        Path sourceDir = Path.of("source");
        Path targetDir = Path.of("target");

        XtfCopyParams params = XtfCopyParams.of(sourceDir, targetDir, XtfCopyParams.TransferFileType.XTF, fileList);

        assertEquals(sourceDir, params.getSourceDir());
        assertEquals(targetDir, params.getTargetDir());
        assertEquals(XtfCopyParams.TransferFileType.XTF, params.getTransferFileType());
        assertEquals(fileList, params.getTransferFileList());
    }

    @Test
    void rejectsNullTransferFileType() {
        TransferFileList fileList = TransferFileList.of("alpha");

        assertThrows(NullPointerException.class,
                () -> XtfCopyParams.of(Path.of("source"), Path.of("target"), null, fileList));
    }

    @Test
    void rejectsNullTransferFileList() {
        assertThrows(NullPointerException.class,
                () -> XtfCopyParams.of(Path.of("source"), Path.of("target"), XtfCopyParams.TransferFileType.ITF, null));
    }
}
