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

    private Object iliFile = null;
    private Object iliMetaAttrs = null;
    private Boolean oneGeomPerTable = false;
    private Boolean setupPgExt = false;
    private Object dropscript = null;
    private Object createscript = null;
    private Object metaConfig = null;
    private String defaultSrsCode = null;
    private Boolean createSingleEnumTab = false;
    private Boolean createEnumTabs = false;
    private Boolean createEnumTxtCol = false;
    private Boolean createEnumColAsItfCode = false;
    private Boolean createEnumTabsWithId = false;
    private Boolean createImportTabs = false;
    private Boolean beautifyEnumDispName = false;
    private Boolean noSmartMapping = false;
    private Boolean smart1Inheritance = false;
    private Boolean smart2Inheritance = false;
    private Boolean coalesceCatalogueRef = false;
    private Boolean coalesceMultiSurface = false;
    private Boolean coalesceMultiLine = false;
    private Boolean expandMultilingual = false;
    private Boolean coalesceJson = false;
    private Boolean coalesceArray = false;
    private Boolean createTypeConstraint = false;
    private Boolean createFk = false;
    private Boolean createFkIdx = false;
    private Boolean createUnique = false;
    private Boolean createNumChecks = false;
    private Boolean createTextChecks = false;
    private Boolean createDateTimeChecks = false;
    private Boolean createStdCols = false;
    private String t_id_Name = null;
    private Long idSeqMin = null;
    private Long idSeqMax = null;
    private Boolean createTypeDiscriminator = false;
    private Boolean createGeomIdx = false;
    private Boolean disableNameOptimization = false;
    private Boolean nameByTopic = false;
    private Integer maxNameLength = null;
    private Boolean sqlEnableNull = false;
    private Boolean sqlColsAsText = false;
    private Boolean sqlExtRefCols = false;
    private Boolean keepAreaRef = false;
    private Boolean createTidCol = false;
    private Boolean createBasketCol = false;
    private Boolean createDatasetCol = false;
    private String translation = null;
    private Boolean createMetaInfo = false;
    private String defaultSrsAuth = null;

    @InputFile
    @Optional
    public Object getIliFile() {
        return iliFile;
    }

    @InputFile
    @Optional
    public Object getIliMetaAttrs() {
        return iliMetaAttrs;
    }

    @Input
    @Optional
    public Boolean isOneGeomPerTable() {
        return oneGeomPerTable;
    }

    @Input
    @Optional
    public Boolean isSetupPgExt() {
        return setupPgExt;
    }

    @OutputFile
    @Optional
    public Object getDropscript() {
        return dropscript;
    }

    @OutputFile
    @Optional
    public Object getCreatescript() {
        return createscript;
    }

    @Input
    @Optional
    public Object getMetaConfig() {
        return metaConfig;
    }

    @Input
    @Optional
    public String getDefaultSrsAuth() {
        return defaultSrsAuth;
    }

    @Input
    @Optional
    public String getDefaultSrsCode() {
        return defaultSrsCode;
    }

    @Input
    @Optional
    public Boolean isCreateSingleEnumTab() {
        return createSingleEnumTab;
    }

    @Input
    @Optional
    public Boolean isCreateEnumTabs() {
        return createEnumTabs;
    }

    @Input
    @Optional
    public Boolean isCreateEnumTxtCol() {
        return createEnumTxtCol;
    }

    @Input
    @Optional
    public Boolean isCreateEnumColAsItfCode() {
        return createEnumColAsItfCode;
    }

    @Input
    @Optional
    public Boolean isCreateEnumTabsWithId() {
        return createEnumTabsWithId;
    }

    @Input
    @Optional
    public Boolean isCreateImportTabs() {
        return createImportTabs;
    }

    @Input
    @Optional
    public Boolean isBeautifyEnumDispName() {
        return beautifyEnumDispName;
    }

    @Input
    @Optional
    public Boolean isNoSmartMapping() {
        return noSmartMapping;
    }

    @Input
    @Optional
    public Boolean isSmart1Inheritance() {
        return smart1Inheritance;
    }

    @Input
    @Optional
    public Boolean isSmart2Inheritance() {
        return smart2Inheritance;
    }

    @Input
    @Optional
    public Boolean isCoalesceCatalogueRef() {
        return coalesceCatalogueRef;
    }
    @Input
    @Optional
    public Boolean isCoalesceMultiSurface() {
        return coalesceMultiSurface;
    }
    @Input
    @Optional
    public Boolean isCoalesceMultiLine() {
        return coalesceMultiLine;
    }
    @Input
    @Optional
    public Boolean isExpandMultilingual() {
        return expandMultilingual;
    }
    @Input
    @Optional
    public Boolean isCoalesceJson() {
        return coalesceJson;
    }
    @Input
    @Optional
    public Boolean isCoalesceArray() {
        return coalesceArray;
    }
    @Input
    @Optional
    public Boolean isCreateTypeConstraint() {
        return createTypeConstraint;
    }
    @Input
    @Optional
    public Boolean isCreateFk() {
        return createFk;
    }
    @Input
    @Optional
    public Boolean isCreateFkIdx() {
        return createFkIdx;
    }
    @Input
    @Optional
    public Boolean isCreateUnique() {
        return createUnique;
    }
    @Input
    @Optional
    public Boolean isCreateNumChecks() {
        return createNumChecks;
    }
    @Input
    @Optional
    public Boolean isCreateTextChecks() {
        return createTextChecks;
    }
    @Input
    @Optional
    public Boolean isCreateDateTimeChecks() {
        return createDateTimeChecks;
    }
    @Input
    @Optional
    public Boolean isCreateStdCols() {
        return createStdCols;
    }
    @Input
    @Optional
    public String getT_id_Name() {
        return t_id_Name;
    }
    @Input
    @Optional
    public Long getIdSeqMin() {
        return idSeqMin;
    }
    @Input
    @Optional
    public Long getIdSeqMax() {
        return idSeqMax;
    }
    @Input
    @Optional
    public Boolean isCreateTypeDiscriminator() {
        return createTypeDiscriminator;
    }
    @Input
    @Optional
    public Boolean isCreateGeomIdx() {
        return createGeomIdx;
    }
    @Input
    @Optional
    public Boolean isDisableNameOptimization() {
        return disableNameOptimization;
    }
    @Input
    @Optional
    public Boolean isNameByTopic() {
        return nameByTopic;
    }
    @Input
    @Optional
    public Integer getMaxNameLength() {
        return maxNameLength;
    }
    @Input
    @Optional
    public Boolean isSqlEnableNull() {
        return sqlEnableNull;
    }
    @Input
    @Optional
    public Boolean isSqlColsAsText() {
        return sqlColsAsText;
    }
    @Input
    @Optional
    public Boolean isSqlExtRefCols() {
        return sqlExtRefCols;
    }
    @Input
    @Optional
    public Boolean isKeepAreaRef() {
        return keepAreaRef;
    }
    @Input
    @Optional
    public Boolean isCreateTidCol() {
        return createTidCol;
    }

    @Input
    @Optional
    public Boolean isCreateBasketCol() {
        return createBasketCol;
    }

    @Input
    @Optional
    public Boolean isCreateDatasetCol() {
        return createDatasetCol;
    }

    @Input
    @Optional
    public String getTranslation() {
        return translation;
    }

    @Input
    @Optional
    public Boolean isCreateMetaInfo() {
        return createMetaInfo;
    }


    @TaskAction
    public void importSchema() {
        Config settings = createConfig();
        int function = Config.FC_SCHEMAIMPORT;
        String iliFilename = null;
        if (iliFile == null) {
        } else {
            if (iliFile instanceof String
                    && ch.ehi.basics.view.GenericFileFilter.getFileExtension((String) iliFile) == null) {
                iliFilename = (String) iliFile;
            } else {
                iliFilename = this.getProject().file(iliFile).getPath();
            }
        }
        settings.setXtffile(iliFilename);
        
        if (iliMetaAttrs != null) {
            String iliMetaAttrsFilename = this.getProject().file(iliMetaAttrs).getPath();
            settings.setIliMetaAttrsFile(iliMetaAttrsFilename);
        }
        
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
        if (metaConfig != null) {
            settings.setMetaConfigFile(this.getProject().file(metaConfig).getPath());
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
        if (createEnumTabsWithId) {
            settings.setCreateEnumDefs(Config.CREATE_ENUM_DEFS_MULTI_WITH_ID);
        }
        if (createImportTabs) {
            settings.setCreateImportTabs(true);
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
        if (coalesceJson) {
            settings.setJsonTrafo(settings.JSON_TRAFO_COALESCE);
        }
        if (coalesceArray) {
            settings.setArrayTrafo(settings.ARRAY_TRAFO_COALESCE);
        }
        if (createTypeConstraint) {
            settings.setCreateTypeConstraint(true);
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
        if (createTextChecks) {
            settings.setCreateTextChecks(true);
        }
        if (createDateTimeChecks) {
            settings.setCreateDateTimeChecks(true);
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
        if (sqlColsAsText) {
            settings.setSqlColsAsText(settings.SQL_COLS_AS_TEXT_ENABLE);
        }
        if (sqlExtRefCols) {
            settings.setSqlExtRefCols(settings.SQL_EXTREF_ENABLE);
        }
        if (keepAreaRef) {
            settings.setAreaRef(settings.AREA_REF_KEEP);
        }
        if (createTidCol) {
            settings.setTidHandling(settings.TID_HANDLING_PROPERTY);
        }
        if (createBasketCol) {
            settings.setBasketHandling(settings.BASKET_HANDLING_READWRITE);
        }
        if (createDatasetCol) {
            settings.setCreateDatasetCols(settings.CREATE_DATASET_COL);
        }
        if (translation != null) {
            settings.setIli1Translation(translation);
        }
        if (createMetaInfo) {
            settings.setCreateMetaInfo(true);
        }
    }
}
