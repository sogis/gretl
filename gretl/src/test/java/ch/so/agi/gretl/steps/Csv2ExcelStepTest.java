package ch.so.agi.gretl.steps;

import ch.ehi.basics.settings.Settings;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.so.agi.gretl.testutil.TestUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.interlis2.validator.Validator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class Csv2ExcelStepTest {
   
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void encoding_iso_8859_1_Ok() throws Exception {
        // Prepare
        Settings settings = new Settings();
        settings.setValue(IoxWkfConfig.SETTING_FIRSTLINE, IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER);
        settings.setValue(IoxWkfConfig.SETTING_VALUEDELIMITER, null);            
        settings.setValue(IoxWkfConfig.SETTING_VALUESEPARATOR, ";");            
        settings.setValue(CsvReader.ENCODING, "ISO-8859-1");
        settings.setValue(Validator.SETTING_MODELNAMES, "SO_HBA_Gebaeude_20230111");

        File csvFile = TestUtil.getResourceFile(TestUtil.SAP_GEBAEUDE_CSV_PATH);
        Path outputPath = folder.newFolder().toPath();
        
        // Run
        Csv2ExcelStep csv2excelStep = new Csv2ExcelStep();
        csv2excelStep.execute(csvFile.toPath(), outputPath, settings);

        // Validate
        FileInputStream fis = new FileInputStream(Paths.get(outputPath.toFile().getAbsolutePath(), "20230124_sap_Gebaeude.xlsx").toFile());        
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
