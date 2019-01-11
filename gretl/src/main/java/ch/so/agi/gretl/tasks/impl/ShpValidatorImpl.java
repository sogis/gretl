package ch.so.agi.gretl.tasks.impl;

import java.io.File;

import ch.interlis.iox_j.PipelinePool;
import org.interlis2.validator.Validator;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.ioxwkf.shp.ShapeReader;

public class ShpValidatorImpl extends Validator {

    @Override
    protected IoxReader createReader(String filename, TransferDescription td, LogEventFactory errFactory,Settings settings, PipelinePool pool)
            throws IoxException {
        ShapeReader reader=new ShapeReader(new File(filename),settings);
        reader.setModel(td);
        return reader;
    }

}
