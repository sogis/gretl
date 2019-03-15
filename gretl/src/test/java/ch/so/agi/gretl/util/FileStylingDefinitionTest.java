package ch.so.agi.gretl.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class FileStylingDefinitionTest {    
    @Test 
    public void checkEncoding_Ok() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("awjf_biotopbaeume_pub_biotopbaeume_biotopbaum_ok.sql").getFile());
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
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public void fileWithBOMThrowsException() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("query_with_bom.sql").getFile());
        try {
            FileStylingDefinition.checkForBOMInFile(inputfile);
        } catch (GretlException e) {
            Assert.assertEquals("file with unallowed BOM", e.getType());
        }
    }

    @Test
    public void passingOnFileWithoutBOM() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("test_utf8.txt").getFile());
        FileStylingDefinition.checkForBOMInFile(inputfile);
    }
}
