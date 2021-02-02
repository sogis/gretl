package ch.so.agi.gretl.tasks.impl;

import java.io.File;

import org.interlis2.validator.Validator;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iox.IoxException;
import ch.interlis.iox.IoxReader;
import ch.interlis.iox_j.PipelinePool;
import ch.interlis.iox_j.logging.LogEventFactory;
import ch.interlis.ioxwkf.dbtools.IoxWkfConfig;
import ch.interlis.ioxwkf.gpkg.GeoPackageReader;

public class GpkgValidatorImpl extends Validator {
    @Override
    protected IoxReader createReader(String filename, TransferDescription td,LogEventFactory errFactory,Settings settings,PipelinePool pool) throws IoxException {
        System.err.println("*x***********c****");

        GeoPackageReader reader = new GeoPackageReader(new File(filename), settings.getValue(IoxWkfConfig.SETTING_GPKGTABLE), settings);
        reader.setModel(td);
        return reader;
    }

}
