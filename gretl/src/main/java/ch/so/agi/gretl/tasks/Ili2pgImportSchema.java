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
    public abstract Property<Boolean> getIsOneGeomPerTable();

    @Input
    @Optional
    public abstract Property<Boolean> getIsSetupPgExt();

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
    public abstract Property<Boolean> getIsCreateSingleEnumTab();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateEnumTabs();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateEnumTxtCol();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateEnumColAsItfCode();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateEnumTabsWithId();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateImportTabs();

    @Input
    @Optional
    public abstract Property<Boolean> getIsBeautifyEnumDispName();

    @Input
    @Optional
    public abstract Property<Boolean> getIsNoSmartMapping();

    @Input
    @Optional
    public abstract Property<Boolean> getIsSmart1Inheritance();

    @Input
    @Optional
    public abstract Property<Boolean> getIsSmart2Inheritance();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCoalesceCatalogueRef();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCoalesceMultiSurface();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCoalesceMultiLine();

    @Input
    @Optional
    public abstract Property<Boolean> getIsExpandMultilingual();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCoalesceJson();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCoalesceArray();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateTypeConstraint();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateFk();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateFkIdx();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateUnique();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateNumChecks();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateTextChecks();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateDateTimeChecks();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateStdCols();
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
    public abstract Property<Boolean> getIsCreateTypeDiscriminator();
    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateGeomIdx();
    @Input
    @Optional
    public abstract Property<Boolean> getIsDisableNameOptimization();
    @Input
    @Optional
    public abstract Property<Boolean> getIsNameByTopic();
    @Input
    @Optional
    public abstract Property<Integer> getMaxNameLength();
    @Input
    @Optional
    public abstract Property<Boolean> getIsSqlEnableNull();
    @Input
    @Optional
    public abstract Property<Boolean> getIsSqlColsAsText();
    @Input
    @Optional
    public abstract Property<Boolean> getIsSqlExtRefCols();
    @Input
    @Optional
    public abstract Property<Boolean> getIsKeepAreaRef();
    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateTidCol();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateBasketCol();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateDatasetCol();

    @Input
    @Optional
    public abstract Property<String> getTranslation();

    @Input
    @Optional
    public abstract Property<Boolean> getIsCreateMetaInfo();

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
        if (getIsOneGeomPerTable().get()) {
            settings.setOneGeomPerTable(true);
        }
        if (getIsSetupPgExt().get()) {
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
        if (getIsCreateSingleEnumTab().get()) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_SINGLE);
        }
        if (getIsCreateEnumTabs().get()) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_MULTI);
        }
        if (getIsCreateEnumTxtCol().get()) {
            settings.setCreateEnumCols(settings.CREATE_ENUM_TXT_COL);
        }
        if (getIsCreateEnumColAsItfCode().get()) {
            settings.setCreateEnumColAsItfCode(settings.CREATE_ENUMCOL_AS_ITFCODE_YES);
        }
        if (getIsCreateEnumTabsWithId().get()) {
            settings.setCreateEnumDefs(Config.CREATE_ENUM_DEFS_MULTI_WITH_ID);
        }
        if (getIsCreateImportTabs().get()) {
            settings.setCreateImportTabs(true);
        }
        if (getIsBeautifyEnumDispName().get()) {
            settings.setBeautifyEnumDispName(settings.BEAUTIFY_ENUM_DISPNAME_UNDERSCORE);
        }
        if (getIsNoSmartMapping().get()) {
            Ili2db.setNoSmartMapping(settings);
        }
        if (getIsSmart1Inheritance().get()) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART1);
        }
        if (getIsSmart2Inheritance().get()) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART2);
        }
        if (getIsCoalesceCatalogueRef().get()) {
            settings.setCatalogueRefTrafo(settings.CATALOGUE_REF_TRAFO_COALESCE);
        }
        if (getIsCoalesceMultiSurface().get()) {
            settings.setMultiSurfaceTrafo(settings.MULTISURFACE_TRAFO_COALESCE);
        }
        if (getIsCoalesceMultiLine().get()) {
            settings.setMultiLineTrafo(settings.MULTILINE_TRAFO_COALESCE);
        }
        if (getIsExpandMultilingual().get()) {
            settings.setMultilingualTrafo(settings.MULTILINGUAL_TRAFO_EXPAND);
        }
        if (getIsCoalesceJson().get()) {
            settings.setJsonTrafo(settings.JSON_TRAFO_COALESCE);
        }
        if (getIsCoalesceArray().get()) {
            settings.setArrayTrafo(settings.ARRAY_TRAFO_COALESCE);
        }
        if (getIsCreateTypeConstraint().get()) {
            settings.setCreateTypeConstraint(true);
        }
        if (getIsCreateFk().get()) {
            settings.setCreateFk(settings.CREATE_FK_YES);
        }
        if (getIsCreateFkIdx().get()) {
            settings.setCreateFkIdx(settings.CREATE_FKIDX_YES);
        }
        if (getIsCreateUnique().get()) {
            settings.setCreateUniqueConstraints(true);
        }
        if (getIsCreateNumChecks().get()) {
            settings.setCreateNumChecks(true);
        }
        if (getIsCreateTextChecks().get()) {
            settings.setCreateTextChecks(true);
        }
        if (getIsCreateDateTimeChecks().get()) {
            settings.setCreateDateTimeChecks(true);
        }
        if (getIsCreateStdCols().get()) {
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
        if (getIsCreateTypeDiscriminator().get()) {
            settings.setCreateTypeDiscriminator(settings.CREATE_TYPE_DISCRIMINATOR_ALWAYS);
        }
        if (getIsCreateGeomIdx().get()) {
            settings.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);
        }
        if (getIsDisableNameOptimization().get()) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_DISABLE);
        }
        if (getIsNameByTopic().get()) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_TOPIC);
        }
        if (getMaxNameLength().isPresent()) {
            settings.setMaxSqlNameLength(getMaxNameLength().get().toString());
        }
        if (getIsSqlEnableNull().get()) {
            settings.setSqlNull(settings.SQL_NULL_ENABLE);
        }
        if (getIsSqlColsAsText().get()) {
            settings.setSqlColsAsText(settings.SQL_COLS_AS_TEXT_ENABLE);
        }
        if (getIsSqlExtRefCols().get()) {
            settings.setSqlExtRefCols(settings.SQL_EXTREF_ENABLE);
        }
        if (getIsKeepAreaRef().get()) {
            settings.setAreaRef(settings.AREA_REF_KEEP);
        }
        if (getIsCreateTidCol().get()) {
            settings.setTidHandling(settings.TID_HANDLING_PROPERTY);
        }
        if (getIsCreateBasketCol().get()) {
            settings.setBasketHandling(settings.BASKET_HANDLING_READWRITE);
        }
        if (getIsCreateDatasetCol().get()) {
            settings.setCreateDatasetCols(settings.CREATE_DATASET_COL);
        }
        if (getTranslation().isPresent()) {
            settings.setIli1Translation(getTranslation().get());
        }
        if (getIsCreateMetaInfo().get()) {
            settings.setCreateMetaInfo(true);
        }
    }
}
