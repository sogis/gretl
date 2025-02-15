package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Csv2ExcelTest {

    @Test
    public void convertCsv_Ok() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Csv2Excel");
        new File(projectDirectory.getAbsolutePath() + "/20230124_sap_Gebaeude.xlsx").delete();

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory);

        // Validate
        FileInputStream fis = new FileInputStream(projectDirectory.getAbsolutePath() + "/20230124_sap_Gebaeude.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        assertEquals(7, headerRow.getLastCellNum());

        Row dataRow = sheet.getRow(1);
        assertEquals(7, dataRow.getLastCellNum());

        assertEquals(308, sheet.getLastRowNum());

        workbook.close();
        fis.close();
    }
    
    @Test
    public void convertEmptyCsv_Ok() throws Exception {
        // Prepare
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Csv2ExcelEmptyFile");
        new File(projectDirectory.getAbsolutePath() + "/superflous_publication_formats.xlsx").delete();

        // Execute test
        IntegrationTestUtil.executeTestRunner(projectDirectory);

        // Validate
        FileInputStream fis = new FileInputStream(projectDirectory.getAbsolutePath() + "/superflous_publication_formats.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        assertEquals("egid", headerRow.getCell(0).getStringCellValue());
        assertEquals("xkoordinaten", headerRow.getCell(1).getStringCellValue());

        assertEquals(0, sheet.getLastRowNum());

        workbook.close();
        fis.close();
    }
}
