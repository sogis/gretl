package ch.so.agi.gretl.tasks;


import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;


public class Ili2pgImportSchema extends Ili2pgAbstractTask {
    @InputFile 
    @Optional
    public Object iliFile=null;
    @Input
    @Optional
    public boolean oneGeomPerTable = false;
    @Input
    @Optional
    public boolean setupPgExt = false;
    @OutputFile
    @Optional
    public Object dropscript = null;
    @OutputFile
    @Optional
    public Object createscript = null;
    @Input
    @Optional
    public String defaultSrsAuth = null;
    @Input
    @Optional
    public String defaultSrsCode = null;
    @Input
    @Optional
    public boolean createSingleEnumTab = false;
    @Input
    @Optional
    public boolean createEnumTabs = false;
    @Input
    @Optional
    public boolean createEnumTxtCol = false;
    @Input
    @Optional
    public boolean createEnumColAsItfCode = false;
    @Input
    @Optional
    public boolean beautifyEnumDispName = false;
    @Input
    @Optional
    public boolean noSmartMapping = false;
    @Input
    @Optional
    public boolean smart1Inheritance = false;
    @Input
    @Optional
    public boolean smart2Inheritance = false;
    @Input
    @Optional
    public boolean coalesceCatalogueRef = false;
    @Input
    @Optional
    public boolean coalesceMultiSurface = false;
    @Input
    @Optional
    public boolean coalesceMultiLine = false;
    @Input
    @Optional
    public boolean expandMultilingual = false;
    @Input
    @Optional
    public boolean createFk = false;
    @Input
    @Optional
    public boolean createFkIdx = false;
    @Input
    @Optional
    public boolean createUnique = false;
    @Input
    @Optional
    public boolean createNumChecks = false;
    @Input
    @Optional
    public boolean createStdCols = false;
    @Input
    @Optional
    public String t_id_Name = null;
    @Input
    @Optional
    public Long idSeqMin = null;
    @Input
    @Optional
    public Long idSeqMax = null;
    @Input
    @Optional
    public boolean createTypeDiscriminator = false;
    @Input
    @Optional
    public boolean createGeomIdx = false;
    @Input
    @Optional
    public boolean disableNameOptimization = false;
    @Input
    @Optional
    public boolean nameByTopic = false;
    @Input
    @Optional
    public Integer maxNameLength = null;
    @Input
    @Optional
    public boolean sqlEnableNull = false;
    @Input
    @Optional
    public boolean keepAreaRef = false;
    @Input
    @Optional
    public boolean importTid = false;
    @Input
    @Optional
    public boolean createBasketCol = false;
    @Input
    @Optional
    public boolean createDatasetCol = false;
    @Input
    @Optional
    public boolean ver4_translation = false;
    @Input
    @Optional
    public String translation = null;
    @Input
    @Optional
    public boolean createMetaInfo = false;
    
    @TaskAction
    public void importSchema()
    {
        Config settings=createConfig();
        int function=Config.FC_SCHEMAIMPORT;
        String iliFilename=null;
        if(iliFile==null) {
        }else {
            if(iliFile instanceof String && ch.ehi.basics.view.GenericFileFilter.getFileExtension((String) iliFile)==null) {
                iliFilename=(String)iliFile;
            }else {
                iliFilename=this.getProject().file(iliFile).getPath();
            }
        }
        settings.setXtffile(iliFilename);
        init(settings);
        run(function, settings);
    }
    private void init(Config settings) {
        if (oneGeomPerTable) {
            settings.setOneGeomPerTable(true);
        }
        if (setupPgExt) {
            settings.setSetupPgExt(true);
        }
        if (dropscript != null) {
            settings.setDropscript(this.getProject().file(dropscript).getPath());
        }
        if (createscript != null) {
            settings.setCreatescript(this.getProject().file(createscript).getPath());
        }
        if (defaultSrsAuth != null) {
            String auth = defaultSrsAuth;
            if (auth.equalsIgnoreCase("NULL")) {
                auth = null;
            }
            settings.setDefaultSrsAuthority(auth);
        }
        if (defaultSrsCode != null) {
            settings.setDefaultSrsCode(defaultSrsCode);
        }
        if (createSingleEnumTab) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_SINGLE);
        }
        if (createEnumTabs) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_MULTI);
        }
        if (createEnumTxtCol) {
            settings.setCreateEnumCols(settings.CREATE_ENUM_TXT_COL);
        }
        if (createEnumColAsItfCode) {
            settings.setCreateEnumColAsItfCode(settings.CREATE_ENUMCOL_AS_ITFCODE_YES);
        }
        if (beautifyEnumDispName) {
            settings.setBeautifyEnumDispName(settings.BEAUTIFY_ENUM_DISPNAME_UNDERSCORE);
        }
        if (noSmartMapping) {
            Ili2db.setNoSmartMapping(settings);
        }
        if (smart1Inheritance) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART1);
        }
        if (smart2Inheritance) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART2);
        }
        if (coalesceCatalogueRef) {
            settings.setCatalogueRefTrafo(settings.CATALOGUE_REF_TRAFO_COALESCE);
        }
        if (coalesceMultiSurface) {
            settings.setMultiSurfaceTrafo(settings.MULTISURFACE_TRAFO_COALESCE);
        }
        if (coalesceMultiLine) {
            settings.setMultiLineTrafo(settings.MULTILINE_TRAFO_COALESCE);
        }
        if (expandMultilingual) {
            settings.setMultilingualTrafo(settings.MULTILINGUAL_TRAFO_EXPAND);
        }
        if (createFk) {
            settings.setCreateFk(settings.CREATE_FK_YES);
        }
        if (createFkIdx) {
            settings.setCreateFkIdx(settings.CREATE_FKIDX_YES);
        }
        if (createUnique) {
            settings.setCreateUniqueConstraints(true);
        }
        if (createNumChecks) {
            settings.setCreateNumChecks(true);
        }
        if (createStdCols) {
            settings.setCreateStdCols(settings.CREATE_STD_COLS_ALL);
        }
        if (t_id_Name != null) {
            settings.setColT_ID(t_id_Name);
        }
        if (idSeqMin != null) {
            settings.setMinIdSeqValue(idSeqMin);
        }
        if (idSeqMax != null) {
            settings.setMaxIdSeqValue(idSeqMax);
        }
        if (createTypeDiscriminator) {
            settings.setCreateTypeDiscriminator(settings.CREATE_TYPE_DISCRIMINATOR_ALWAYS);
        }
        if (createGeomIdx) {
            settings.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);
        }
        if (disableNameOptimization) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_DISABLE);
        }
        if (nameByTopic) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_TOPIC);
        }
        if (maxNameLength != null) {
            settings.setMaxSqlNameLength(maxNameLength.toString());
        }
        if (sqlEnableNull) {
            settings.setSqlNull(settings.SQL_NULL_ENABLE);
        }
        if (keepAreaRef) {
            settings.setAreaRef(settings.AREA_REF_KEEP);
        }
        if (importTid) {
            settings.setTidHandling(settings.TID_HANDLING_PROPERTY);
        }
        if (createBasketCol) {
            settings.setBasketHandling(settings.BASKET_HANDLING_READWRITE);
        }
        if (createDatasetCol) {
            settings.setCreateDatasetCols(settings.CREATE_DATASET_COL);
        }
        if (ver4_translation) {
            settings.setVer4_translation(true);
        }
        if (translation != null) {
            settings.setIli1Translation(translation);
        }
        if (createMetaInfo) {
            settings.setCreateMetaInfo(true);
        }

    }
}

