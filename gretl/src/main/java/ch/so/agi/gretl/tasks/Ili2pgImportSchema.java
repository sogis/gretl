package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class Ili2pgImportSchema extends Ili2pgAbstractTask {
    @InputFile
    @Optional
    public abstract Property<Object> getIliFile();

    @InputFile
    @Optional
    public abstract Property<Object> getIliMetaAttrs();

    @Input
    @Optional
    public abstract Property<Boolean> isOneGeomPerTable();

    @Input
    @Optional
    public abstract Property<Boolean> isSetupPgExt();

    @OutputFile
    @Optional
    public abstract Property<Object> getDropscript();

    @OutputFile
    @Optional
    public abstract Property<Object> getCreatescript();

    @Input
    @Optional
    public abstract Property<Object> getMetaConfig();

    @Input
    @Optional
    public abstract Property<String> getDefaultSrsAuth();

    @Input
    @Optional
    public abstract Property<String> getDefaultSrsCode();

    @Input
    @Optional
    public abstract Property<Boolean> isCreateSingleEnumTab();

    @Input
    @Optional
    public abstract Property<Boolean> isCreateEnumTabs();

    @Input
    @Optional
    public abstract Property<Boolean> isCreateEnumTxtCol();

    @Input
    @Optional
    public abstract Property<Boolean> isCreateEnumColAsItfCode();

    @Input
    @Optional
    public abstract Property<Boolean> isCreateEnumTabsWithId();

    @Input
    @Optional
    public abstract Property<Boolean> isCreateImportTabs();

    @Input
    @Optional
    public abstract Property<Boolean> isBeautifyEnumDispName();

    @Input
    @Optional
    public abstract Property<Boolean> isNoSmartMapping();

    @Input
    @Optional
    public abstract Property<Boolean> isSmart1Inheritance();

    @Input
    @Optional
    public abstract Property<Boolean> isSmart2Inheritance();

    @Input
    @Optional
    public abstract Property<Boolean> isCoalesceCatalogueRef();
    @Input
    @Optional
    public abstract Property<Boolean> isCoalesceMultiSurface();
    @Input
    @Optional
    public abstract Property<Boolean> isCoalesceMultiLine();
    @Input
    @Optional
    public abstract Property<Boolean> isExpandMultilingual();
    @Input
    @Optional
    public abstract Property<Boolean> isCoalesceJson();
    @Input
    @Optional
    public abstract Property<Boolean> isCoalesceArray();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateTypeConstraint();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateFk();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateFkIdx();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateUnique();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateNumChecks();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateTextChecks();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateDateTimeChecks();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateStdCols();
    @Input
    @Optional
    public abstract Property<String> getT_id_Name();
    @Input
    @Optional
    public abstract Property<Long> getIdSeqMin();
    @Input
    @Optional
    public abstract Property<Long> getIdSeqMax();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateTypeDiscriminator();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateGeomIdx();
    @Input
    @Optional
    public abstract Property<Boolean> isDisableNameOptimization();
    @Input
    @Optional
    public abstract Property<Boolean> isNameByTopic();
    @Input
    @Optional
    public abstract Property<Integer> getMaxNameLength();
    @Input
    @Optional
    public abstract Property<Boolean> isSqlEnableNull();
    @Input
    @Optional
    public abstract Property<Boolean> isSqlColsAsText();
    @Input
    @Optional
    public abstract Property<Boolean> isSqlExtRefCols();
    @Input
    @Optional
    public abstract Property<Boolean> isKeepAreaRef();
    @Input
    @Optional
    public abstract Property<Boolean> isCreateTidCol();

    @Input
    @Optional
    public abstract Property<Boolean> isCreateBasketCol();

    @Input
    @Optional
    public abstract Property<Boolean> isCreateDatasetCol();

    @Input
    @Optional
    public abstract Property<String> getTranslation();

    @Input
    @Optional
    public abstract Property<Boolean> isCreateMetaInfo();

    @TaskAction
    public void importSchema() {
        Config settings = createConfig();
        int function = Config.FC_SCHEMAIMPORT;
        String iliFilename = null;
        if (getIliFile().isPresent()) {
            Object iliFile = getIliFile().get();
            if (iliFile instanceof String
                    && ch.ehi.basics.view.GenericFileFilter.getFileExtension((String) iliFile) == null) {
                iliFilename = (String) iliFile;
            } else {
                iliFilename = this.getProject().file(iliFile).getPath();
            }
        }
        settings.setXtffile(iliFilename);
        
        if (getIliMetaAttrs().isPresent()) {
            String iliMetaAttrsFilename = this.getProject().file(getIliMetaAttrs().get()).getPath();
            settings.setIliMetaAttrsFile(iliMetaAttrsFilename);
        }
        
        init(settings);
        run(function, settings);
    }

    private void init(Config settings) {
        if (isOneGeomPerTable().get()) {
            settings.setOneGeomPerTable(true);
        }
        if (isSetupPgExt().get()) {
            settings.setSetupPgExt(true);
        }
        if (getDropscript().isPresent()) {
            settings.setDropscript(this.getProject().file(getDropscript().get()).getPath());
        }
        if (getCreatescript().isPresent()) {
            settings.setCreatescript(this.getProject().file(getCreatescript().get()).getPath());
        }
        if (getMetaConfig().isPresent()) {
            settings.setMetaConfigFile(this.getProject().file(getMetaConfig().get()).getPath());
        }
        if (getDefaultSrsAuth().isPresent()) {
            String auth = getDefaultSrsAuth().get();
            if (auth.equalsIgnoreCase("NULL")) {
                auth = null;
            }
            settings.setDefaultSrsAuthority(auth);
        }
        if (getDefaultSrsCode().isPresent()) {
            settings.setDefaultSrsCode(getDefaultSrsCode().get());
        }
        if (isCreateSingleEnumTab().get()) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_SINGLE);
        }
        if (isCreateEnumTabs().get()) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_MULTI);
        }
        if (isCreateEnumTxtCol().get()) {
            settings.setCreateEnumCols(settings.CREATE_ENUM_TXT_COL);
        }
        if (isCreateEnumColAsItfCode().get()) {
            settings.setCreateEnumColAsItfCode(settings.CREATE_ENUMCOL_AS_ITFCODE_YES);
        }
        if (isCreateEnumTabsWithId().get()) {
            settings.setCreateEnumDefs(Config.CREATE_ENUM_DEFS_MULTI_WITH_ID);
        }
        if (isCreateImportTabs().get()) {
            settings.setCreateImportTabs(true);
        }
        if (isBeautifyEnumDispName().get()) {
            settings.setBeautifyEnumDispName(settings.BEAUTIFY_ENUM_DISPNAME_UNDERSCORE);
        }
        if (isNoSmartMapping().get()) {
            Ili2db.setNoSmartMapping(settings);
        }
        if (isSmart1Inheritance().get()) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART1);
        }
        if (isSmart2Inheritance().get()) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART2);
        }
        if (isCoalesceCatalogueRef().get()) {
            settings.setCatalogueRefTrafo(settings.CATALOGUE_REF_TRAFO_COALESCE);
        }
        if (isCoalesceMultiSurface().get()) {
            settings.setMultiSurfaceTrafo(settings.MULTISURFACE_TRAFO_COALESCE);
        }
        if (isCoalesceMultiLine().get()) {
            settings.setMultiLineTrafo(settings.MULTILINE_TRAFO_COALESCE);
        }
        if (isExpandMultilingual().get()) {
            settings.setMultilingualTrafo(settings.MULTILINGUAL_TRAFO_EXPAND);
        }
        if (isCoalesceJson().get()) {
            settings.setJsonTrafo(settings.JSON_TRAFO_COALESCE);
        }
        if (isCoalesceArray().get()) {
            settings.setArrayTrafo(settings.ARRAY_TRAFO_COALESCE);
        }
        if (isCreateTypeConstraint().get()) {
            settings.setCreateTypeConstraint(true);
        }
        if (isCreateFk().get()) {
            settings.setCreateFk(settings.CREATE_FK_YES);
        }
        if (isCreateFkIdx().get()) {
            settings.setCreateFkIdx(settings.CREATE_FKIDX_YES);
        }
        if (isCreateUnique().get()) {
            settings.setCreateUniqueConstraints(true);
        }
        if (isCreateNumChecks().get()) {
            settings.setCreateNumChecks(true);
        }
        if (isCreateTextChecks().get()) {
            settings.setCreateTextChecks(true);
        }
        if (isCreateDateTimeChecks().get()) {
            settings.setCreateDateTimeChecks(true);
        }
        if (isCreateStdCols().get()) {
            settings.setCreateStdCols(settings.CREATE_STD_COLS_ALL);
        }
        if (getT_id_Name().isPresent()) {
            settings.setColT_ID(getT_id_Name().get());
        }
        if (getIdSeqMin().isPresent()) {
            settings.setMinIdSeqValue(getIdSeqMin().get());
        }
        if (getIdSeqMax().isPresent()) {
            settings.setMaxIdSeqValue(getIdSeqMax().get());
        }
        if (isCreateTypeDiscriminator().get()) {
            settings.setCreateTypeDiscriminator(settings.CREATE_TYPE_DISCRIMINATOR_ALWAYS);
        }
        if (isCreateGeomIdx().get()) {
            settings.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);
        }
        if (isDisableNameOptimization().get()) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_DISABLE);
        }
        if (isNameByTopic().get()) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_TOPIC);
        }
        if (getMaxNameLength().isPresent()) {
            settings.setMaxSqlNameLength(getMaxNameLength().get().toString());
        }
        if (isSqlEnableNull().get()) {
            settings.setSqlNull(settings.SQL_NULL_ENABLE);
        }
        if (isSqlColsAsText().get()) {
            settings.setSqlColsAsText(settings.SQL_COLS_AS_TEXT_ENABLE);
        }
        if (isSqlExtRefCols().get()) {
            settings.setSqlExtRefCols(settings.SQL_EXTREF_ENABLE);
        }
        if (isKeepAreaRef().get()) {
            settings.setAreaRef(settings.AREA_REF_KEEP);
        }
        if (isCreateTidCol().get()) {
            settings.setTidHandling(settings.TID_HANDLING_PROPERTY);
        }
        if (isCreateBasketCol().get()) {
            settings.setBasketHandling(settings.BASKET_HANDLING_READWRITE);
        }
        if (isCreateDatasetCol().get()) {
            settings.setCreateDatasetCols(settings.CREATE_DATASET_COL);
        }
        if (getTranslation().isPresent()) {
            settings.setIli1Translation(getTranslation().get());
        }
        if (isCreateMetaInfo().get()) {
            settings.setCreateMetaInfo(true);
        }
    }
}
