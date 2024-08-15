package ch.so.agi.gretl.util;

import ch.so.agi.gretl.testutil.TestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class FileStylingDefinitionTest {
    @TempDir
    public Path folder;

    @Test
    public void checkEncoding_Ok() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("awjf_biotopbaeume_pub_biotopbaeume_biotopbaum_ok.sql").getFile());
        FileStylingDefinition.checkForUtf8(inputfile);
    }
    
    // Empty files are handled elsewhere. But empty files will
    // let the encoding validation stall.
    @Test
    public void checkEncoding_Ok_empty_File() throws Exception {
        File inputfile = TestUtil.createTempFile(folder, "", "query.sql");
        FileStylingDefinition.checkForUtf8(inputfile);
    }
    
    // This file was failing with old test logic.
    // But it is UTF-8 encoded.
    // Workaround was to paste some whitespaces into the file to pass the test.
    @Test
    public void checkEncoding_Ok_was_failing() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("awjf_biotopbaeume_pub_biotopbaeume_biotopbaum_ok_was_failing.sql").getFile());
        FileStylingDefinition.checkForUtf8(inputfile);
    }
    
    @Test 
    public void checkEncoding_Fail() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("test.txt").getFile());
        try {
            FileStylingDefinition.checkForUtf8(inputfile);
            fail();
        } catch (Exception ignored) {}
    }

    @Test
    public void fileWithBOMThrowsException() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("query_with_bom.sql").getFile());
        try {
            FileStylingDefinition.checkForBOMInFile(inputfile);
        } catch (GretlException e) {
            assertEquals("file with unallowed BOM", e.getType());
        }
    }

    @Test
    public void passingOnFileWithoutBOM() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("test_utf8.txt").getFile());
        FileStylingDefinition.checkForBOMInFile(inputfile);
    }
}
