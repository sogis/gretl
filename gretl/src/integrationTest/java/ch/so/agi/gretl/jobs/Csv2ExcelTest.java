package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Csv2ExcelTest {

    @Test
    public void convertCsv_Ok() throws Exception {        
        // Run GRETL task
        GradleVariable[] gvs = null;
        IntegrationTestUtil.runJob("src/integrationTest/jobs/Csv2Excel", gvs);
                
        // Validate result
        FileInputStream fis = new FileInputStream(Paths.get("src/integrationTest/jobs/Csv2Excel", "20230124_sap_Gebaeude.xlsx").toFile());        
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
