package ch.so.agi.gretl.steps;

import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iox.IoxWriter;
import ch.so.agi.gretl.logging.GretlLogger;

/**
 * Reads a directory (incl. subdirectories) with INTERLIS model files an
 * creates an ilimodels.xml file for an INTERLIS model repository.
 *
 * @author Stefan Ziegler
 */
public class IliRepositorizerStep {
    private GretlLogger log;

    private TransferDescription tdRepository = null;
    private IoxWriter ioxWriter = null;
    private final static String ILI_TOPIC="IliRepository09.RepositoryIndex";
    private final static String ILI_CLASS=ILI_TOPIC+".ModelMetadata";
    private final static String ILI_STRUCT_MODELNAME="IliRepository09.ModelName_";
    private final static String BID="b1";

    public IliRepositorizerStep() {}

//    public void build(String outputFileName, String modelsDir) throws Ili2cException, IoxException, IOException {
//
//    }
}
