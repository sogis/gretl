package ch.so.agi.gretl.tasks;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.so.agi.gretl.tasks.impl.Ili2pgAbstractTask;

import java.io.File;

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
    public abstract Property<Boolean> getOneGeomPerTable();

    @Input
    @Optional
    public abstract Property<Boolean> getSetupPgExt();

    @InputFile
    @Optional
    public abstract Property<Object> getDropscript();

    @InputFile
    @Optional
    public abstract Property<Object> getCreatescript();
    
    @Input
    @Optional
    public abstract Property<String> getMetaConfig();

    @Input
    @Optional
    public abstract Property<String> getDefaultSrsAuth();

    @Input
    @Optional
    public abstract Property<String> getDefaultSrsCode();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateSingleEnumTab();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateEnumTabs();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateEnumTxtCol();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateEnumColAsItfCode();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateEnumTabsWithId();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateImportTabs();

    @Input
    @Optional
    public abstract Property<Boolean> getBeautifyEnumDispName();

    @Input
    @Optional
    public abstract Property<Boolean> getNoSmartMapping();

    @Input
    @Optional
    public abstract Property<Boolean> getSmart1Inheritance();

    @Input
    @Optional
    public abstract Property<Boolean> getSmart2Inheritance();

    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceCatalogueRef();

    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceMultiSurface();

    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceMultiLine();

    @Input
    @Optional
    public abstract Property<Boolean> getExpandMultilingual();

    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceJson();

    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceArray();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateTypeConstraint();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateFk();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateFkIdx();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateUnique();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateNumChecks();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateTextChecks();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateDateTimeChecks();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateStdCols();
    
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
    public abstract Property<Boolean> getCreateTypeDiscriminator();
    
    @Input
    @Optional
    public abstract Property<Boolean> getCreateGeomIdx();
    
    @Input
    @Optional
    public abstract Property<Boolean> getDisableNameOptimization();
    
    @Input
    @Optional
    public abstract Property<Boolean> getNameByTopic();
    
    @Input
    @Optional
    public abstract Property<Integer> getMaxNameLength();
    
    @Input
    @Optional
    public abstract Property<Boolean> getSqlEnableNull();
    
    @Input
    @Optional
    public abstract Property<Boolean> getSqlColsAsText();
    
    @Input
    @Optional
    public abstract Property<Boolean> getSqlExtRefCols();
    
    @Input
    @Optional
    public abstract Property<Boolean> getKeepAreaRef();
    
    @Input
    @Optional
    public abstract Property<Boolean> getCreateTidCol();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateBasketCol();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateDatasetCol();

    @Input
    @Optional
    public abstract Property<String> getTranslation();

    @Input
    @Optional
    public abstract Property<Boolean> getCreateMetaInfo();

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
        if (getOneGeomPerTable().getOrElse(false)) {
            settings.setOneGeomPerTable(true);
        }
        if (getSetupPgExt().getOrElse(false)) {
            settings.setSetupPgExt(true);
        }
        if (getDropscript().isPresent()) {
            settings.setDropscript(this.getProject().file(getDropscript().get()).getPath());
        }
        if (getCreatescript().isPresent()) {
            settings.setCreatescript(this.getProject().file(getCreatescript().get()).getPath());
        }
        if (getMetaConfig().isPresent()) {            
            String metaConfigFile = null;
            if (getMetaConfig().get().startsWith("ilidata")) {
                metaConfigFile = getMetaConfig().get();
            } else {
                File file = this.getProject().file(getMetaConfig().get());
                metaConfigFile = file.getAbsolutePath();
            }
            settings.setMetaConfigFile(metaConfigFile);
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
        if (getCreateSingleEnumTab().getOrElse(false)) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_SINGLE);
        }
        if (getCreateEnumTabs().getOrElse(false)) {
            settings.setCreateEnumDefs(settings.CREATE_ENUM_DEFS_MULTI);
        }
        if (getCreateEnumTxtCol().getOrElse(false)) {
            settings.setCreateEnumCols(settings.CREATE_ENUM_TXT_COL);
        }
        if (getCreateEnumColAsItfCode().getOrElse(false)) {
            settings.setCreateEnumColAsItfCode(settings.CREATE_ENUMCOL_AS_ITFCODE_YES);
        }
        if (getCreateEnumTabsWithId().getOrElse(false)) {
            settings.setCreateEnumDefs(Config.CREATE_ENUM_DEFS_MULTI_WITH_ID);
        }
        if (getCreateImportTabs().getOrElse(false)) {
            settings.setCreateImportTabs(true);
        }
        if (getBeautifyEnumDispName().getOrElse(false)) {
            settings.setBeautifyEnumDispName(settings.BEAUTIFY_ENUM_DISPNAME_UNDERSCORE);
        }
        if (getNoSmartMapping().getOrElse(false)) {
            Ili2db.setNoSmartMapping(settings);
        }
        if (getSmart1Inheritance().getOrElse(false)) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART1);
        }
        if (getSmart2Inheritance().getOrElse(false)) {
            settings.setInheritanceTrafo(settings.INHERITANCE_TRAFO_SMART2);
        }
        if (getCoalesceCatalogueRef().getOrElse(false)) {
            settings.setCatalogueRefTrafo(settings.CATALOGUE_REF_TRAFO_COALESCE);
        }
        if (getCoalesceMultiSurface().getOrElse(false)) {
            settings.setMultiSurfaceTrafo(settings.MULTISURFACE_TRAFO_COALESCE);
        }
        if (getCoalesceMultiLine().getOrElse(false)) {
            settings.setMultiLineTrafo(settings.MULTILINE_TRAFO_COALESCE);
        }
        if (getExpandMultilingual().getOrElse(false)) {
            settings.setMultilingualTrafo(settings.MULTILINGUAL_TRAFO_EXPAND);
        }
        if (getCoalesceJson().getOrElse(false)) {
            settings.setJsonTrafo(settings.JSON_TRAFO_COALESCE);
        }
        if (getCoalesceArray().getOrElse(false)) {
            settings.setArrayTrafo(settings.ARRAY_TRAFO_COALESCE);
        }
        if (getCreateTypeConstraint().getOrElse(false)) {
            settings.setCreateTypeConstraint(true);
        }
        if (getCreateFk().getOrElse(false)) {
            settings.setCreateFk(settings.CREATE_FK_YES);
        }
        if (getCreateFkIdx().getOrElse(false)) {
            settings.setCreateFkIdx(settings.CREATE_FKIDX_YES);
        }
        if (getCreateUnique().getOrElse(false)) {
            settings.setCreateUniqueConstraints(true);
        }
        if (getCreateNumChecks().getOrElse(false)) {
            settings.setCreateNumChecks(true);
        }
        if (getCreateTextChecks().getOrElse(false)) {
            settings.setCreateTextChecks(true);
        }
        if (getCreateDateTimeChecks().getOrElse(false)) {
            settings.setCreateDateTimeChecks(true);
        }
        if (getCreateStdCols().getOrElse(false)) {
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
        if (getCreateTypeDiscriminator().getOrElse(false)) {
            settings.setCreateTypeDiscriminator(settings.CREATE_TYPE_DISCRIMINATOR_ALWAYS);
        }
        if (getCreateGeomIdx().getOrElse(false)) {
            settings.setValue(Config.CREATE_GEOM_INDEX, Config.TRUE);
        }
        if (getDisableNameOptimization().getOrElse(false)) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_DISABLE);
        }
        if (getNameByTopic().getOrElse(false)) {
            settings.setNameOptimization(settings.NAME_OPTIMIZATION_TOPIC);
        }
        if (getMaxNameLength().isPresent()) {
            settings.setMaxSqlNameLength(getMaxNameLength().get().toString());
        }
        if (getSqlEnableNull().getOrElse(false)) {
            settings.setSqlNull(settings.SQL_NULL_ENABLE);
        }
        if (getSqlColsAsText().getOrElse(false)) {
            settings.setSqlColsAsText(settings.SQL_COLS_AS_TEXT_ENABLE);
        }
        if (getSqlExtRefCols().getOrElse(false)) {
            settings.setSqlExtRefCols(settings.SQL_EXTREF_ENABLE);
        }
        if (getKeepAreaRef().getOrElse(false)) {
            settings.setAreaRef(settings.AREA_REF_KEEP);
        }
        if (getCreateTidCol().getOrElse(false)) {
            settings.setTidHandling(settings.TID_HANDLING_PROPERTY);
        }
        if (getCreateBasketCol().getOrElse(false)) {
            settings.setBasketHandling(settings.BASKET_HANDLING_READWRITE);
        }
        if (getCreateDatasetCol().getOrElse(false)) {
            settings.setCreateDatasetCols(settings.CREATE_DATASET_COL);
        }
        if (getTranslation().isPresent()) {
            settings.setIli1Translation(getTranslation().get());
        }
        if (getCreateMetaInfo().getOrElse(false)) {
            settings.setCreateMetaInfo(true);
        }
    }
}
