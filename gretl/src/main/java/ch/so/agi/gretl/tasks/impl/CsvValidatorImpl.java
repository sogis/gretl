package ch.so.agi.gretl.tasks.impl;

import java.io.File;

import ch.interlis.iox_j.PipelinePool;
import org.interlis2.validator.Validator;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iom_j.csv.CsvReader;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;

public class CsvValidatorImpl extends Validator {

    @Override
    protected IoxReader createReader(String filename, TransferDescription td, LogEventFactory errFactory, Settings settings, PipelinePool pool)
            throws IoxException {
        CsvReader reader=new CsvReader(new File(filename),settings);
        reader.setModel(td);
        boolean firstLineIsHeader=false;
        {
            String val=settings.getValue(IoxWkfConfig.SETTING_FIRSTLINE);
            if(IoxWkfConfig.SETTING_FIRSTLINE_AS_HEADER.equals(val)) {
                firstLineIsHeader=true;
            }
        }
        reader.setFirstLineIsHeader(firstLineIsHeader);
        char valueDelimiter=IoxWkfConfig.SETTING_VALUEDELIMITER_DEFAULT;
        {
            String val=settings.getValue(IoxWkfConfig.SETTING_VALUEDELIMITER);
            if(val!=null) {
                valueDelimiter=val.charAt(0);
            }
        }
        reader.setValueDelimiter(valueDelimiter);
        char valueSeparator=IoxWkfConfig.SETTING_VALUESEPARATOR_DEFAULT;
        {
            String val=settings.getValue(IoxWkfConfig.SETTING_VALUESEPARATOR);
            if(val!=null) {
                valueSeparator=val.charAt(0);
            }
        }
        reader.setValueSeparator(valueSeparator);
        return reader;
    }

}
