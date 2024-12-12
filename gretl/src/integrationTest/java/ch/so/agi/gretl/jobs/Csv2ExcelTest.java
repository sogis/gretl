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
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Csv2Excel");

        IntegrationTestUtil.executeTestRunner(projectDirectory);

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
}
