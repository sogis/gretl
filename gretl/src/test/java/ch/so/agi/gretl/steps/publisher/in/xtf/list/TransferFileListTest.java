package ch.so.agi.gretl.steps.publisher.in.xtf.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class TransferFileListTest {
    @Test
    void preservesOrderAndTrimsEntries() {
        TransferFileList list = TransferFileList.of("  alpha  ", "beta");

        assertEquals(List.of("alpha", "beta"), list.asList());
    }

    @Test
    void rejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> TransferFileList.of(List.of()));
    }

    @Test
    void rejectsDuplicateEntries() {
        assertThrows(IllegalArgumentException.class, () -> TransferFileList.of(List.of("alpha", "alpha")));
    }

    @Test
    void rejectsPathSeparators() {
        assertThrows(IllegalArgumentException.class, () -> TransferFileList.of(List.of("alpha/beta")));
    }

    @Test
    void rejectsTransferFileSuffixes() {
        assertThrows(IllegalArgumentException.class, () -> TransferFileList.of(List.of("alpha.xtf")));
    }

    @Test
    void isImmutable() {
        TransferFileList list = TransferFileList.of(List.of("alpha"));

        assertThrows(UnsupportedOperationException.class, () -> list.asList().add("beta"));
    }
}
