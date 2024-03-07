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
    private boolean oneGeomPerTable = false;
    private boolean setupPgExt = false;
    private Object dropscript = null;
    private Object createscript = null;
    private Object metaConfig = null;
    private String defaultSrsCode = null;
    private boolean createSingleEnumTab = false;
    private boolean createEnumTabs = false;
    private boolean createEnumTxtCol = false;
    private boolean createEnumColAsItfCode = false;
    private boolean createEnumTabsWithId = false;
    private boolean createImportTabs = false;
    private boolean beautifyEnumDispName = false;
    private boolean noSmartMapping = false;
    private boolean smart1Inheritance = false;
    private boolean smart2Inheritance = false;
    private boolean coalesceCatalogueRef = false;
    private boolean coalesceMultiSurface = false;
    private boolean coalesceMultiLine = false;
    private boolean expandMultilingual = false;
    private boolean coalesceJson = false;
    private boolean coalesceArray = false;
    private boolean createTypeConstraint = false;
    private boolean createFk = false;
    private boolean createFkIdx = false;
    private boolean createUnique = false;
    private boolean createNumChecks = false;
    private boolean createTextChecks = false;
    private boolean createDateTimeChecks = false;
    private boolean createStdCols = false;
    private String t_id_Name = null;
    private Long idSeqMin = null;
    private Long idSeqMax = null;
    private boolean createTypeDiscriminator = false;
    private boolean createGeomIdx = false;
    private boolean disableNameOptimization = false;
    private boolean nameByTopic = false;
    private Integer maxNameLength = null;
    private boolean sqlEnableNull = false;
    private boolean sqlColsAsText = false;
    private boolean sqlExtRefCols = false;
    private boolean keepAreaRef = false;
    private boolean createTidCol = false;
    private boolean createBasketCol = false;
    private boolean createDatasetCol = false;
    private String translation = null;
    private boolean createMetaInfo = false;
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
    public boolean isOneGeomPerTable() {
        return oneGeomPerTable;
    }

    @Input
    @Optional
    public boolean isSetupPgExt() {
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
    public boolean isCreateSingleEnumTab() {
        return createSingleEnumTab;
    }

    @Input
    @Optional
    public boolean isCreateEnumTabs() {
        return createEnumTabs;
    }

    @Input
    @Optional
    public boolean isCreateEnumTxtCol() {
        return createEnumTxtCol;
    }

    @Input
    @Optional
    public boolean isCreateEnumColAsItfCode() {
        return createEnumColAsItfCode;
    }

    @Input
    @Optional
    public boolean isCreateEnumTabsWithId() {
        return createEnumTabsWithId;
    }

    @Input
    @Optional
    public boolean isCreateImportTabs() {
        return createImportTabs;
    }

    @Input
    @Optional
    public boolean isBeautifyEnumDispName() {
        return beautifyEnumDispName;
    }

    @Input
    @Optional
    public boolean isNoSmartMapping() {
        return noSmartMapping;
    }

    @Input
    @Optional
    public boolean isSmart1Inheritance() {
        return smart1Inheritance;
    }

    @Input
    @Optional
    public boolean isSmart2Inheritance() {
        return smart2Inheritance;
    }

    @Input
    @Optional
    public boolean isCoalesceCatalogueRef() {
        return coalesceCatalogueRef;
    }
    @Input
    @Optional
    public boolean isCoalesceMultiSurface() {
        return coalesceMultiSurface;
    }
    @Input
    @Optional
    public boolean isCoalesceMultiLine() {
        return coalesceMultiLine;
    }
    @Input
    @Optional
    public boolean isExpandMultilingual() {
        return expandMultilingual;
    }
    @Input
    @Optional
    public boolean isCoalesceJson() {
        return coalesceJson;
    }
    @Input
    @Optional
    public boolean isCoalesceArray() {
        return coalesceArray;
    }
    @Input
    @Optional
    public boolean isCreateTypeConstraint() {
        return createTypeConstraint;
    }
    @Input
    @Optional
    public boolean isCreateFk() {
        return createFk;
    }
    @Input
    @Optional
    public boolean isCreateFkIdx() {
        return createFkIdx;
    }
    @Input
    @Optional
    public boolean isCreateUnique() {
        return createUnique;
    }
    @Input
    @Optional
    public boolean isCreateNumChecks() {
        return createNumChecks;
    }
    @Input
    @Optional
    public boolean isCreateTextChecks() {
        return createTextChecks;
    }
    @Input
    @Optional
    public boolean isCreateDateTimeChecks() {
        return createDateTimeChecks;
    }
    @Input
    @Optional
    public boolean isCreateStdCols() {
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
    public boolean isCreateTypeDiscriminator() {
        return createTypeDiscriminator;
    }
    @Input
    @Optional
    public boolean isCreateGeomIdx() {
        return createGeomIdx;
    }
    @Input
    @Optional
    public boolean isDisableNameOptimization() {
        return disableNameOptimization;
    }
    @Input
    @Optional
    public boolean isNameByTopic() {
        return nameByTopic;
    }
    @Input
    @Optional
    public Integer getMaxNameLength() {
        return maxNameLength;
    }
    @Input
    @Optional
    public boolean isSqlEnableNull() {
        return sqlEnableNull;
    }
    @Input
    @Optional
    public boolean isSqlColsAsText() {
        return sqlColsAsText;
    }
    @Input
    @Optional
    public boolean isSqlExtRefCols() {
        return sqlExtRefCols;
    }
    @Input
    @Optional
    public boolean isKeepAreaRef() {
        return keepAreaRef;
    }
    @Input
    @Optional
    public boolean isCreateTidCol() {
        return createTidCol;
    }

    @Input
    @Optional
    public boolean isCreateBasketCol() {
        return createBasketCol;
    }

    @Input
    @Optional
    public boolean isCreateDatasetCol() {
        return createDatasetCol;
    }

    @Input
    @Optional
    public String getTranslation() {
        return translation;
    }

    @Input
    @Optional
    public boolean isCreateMetaInfo() {
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
