package ch.so.agi.gretl.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class FileStylingDefinitionTest {

    @Test
    public void wrongEncodingThrowsException() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("test.txt").getFile());
        try {
            FileStylingDefinition.checkForUtf8(inputfile);
            Assert.fail();
        } catch (Exception e) {

        }
    }

    @Test
    public void rightEncoding() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputfile = new File(classLoader.getResource("test_utf8.txt").getFile());
        FileStylingDefinition.checkForUtf8(inputfile);

    }

    @Test
    public void FileWithBOMThrowsException() throws Exception {
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
