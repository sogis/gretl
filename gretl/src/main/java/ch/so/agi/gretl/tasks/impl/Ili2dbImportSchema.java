package ch.so.agi.gretl.tasks.impl;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;

public abstract class Ili2dbImportSchema extends Ili2dbAbstractTask {
    
    /**
     * Name der ili-Datei, die gelesen werden soll.
     */
    @InputFile
    @Optional
    public abstract Property<File> getIliFile();

    /**
     * Entspricht der ili2db-Option `--iliMetaAttrs`.
     */    
    @InputFile
    @Optional
    public abstract Property<File> getIliMetaAttrs();

    /**
     * Entspricht der ili2db-Option `--oneGeomPerTable`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getOneGeomPerTable();

    /**
     * Entspricht der ili2db-Option `--setupPgExt`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSetupPgExt();

    /**
     * Entspricht der ili2db-Option `--dropscript`.
     */
    @InputFile
    @Optional
    public abstract Property<File> getDropscript();

    /**
     * Entspricht der ili2db-Option `--createscript`.
     */
    @InputFile
    @Optional
    public abstract Property<File> getCreatescript();
    
    /**
     * Entspricht der ili2db-Option `--metaConfig`.
     */
    @Input
    @Optional
    public abstract Property<String> getMetaConfig();

    /**
     * Entspricht der ili2db-Option `--defaultSrsAuth`.
     */
    @Input
    @Optional
    public abstract Property<String> getDefaultSrsAuth();

    /**
     * Entspricht der ili2db-Option `--defaultSrsCode`.
     */
    @Input
    @Optional
    public abstract Property<String> getDefaultSrsCode();

    /**
     * Entspricht der ili2db-Option `--createSingleEnumTab`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateSingleEnumTab();

    /**
     * Entspricht der ili2db-Option `--createEnumTabs`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateEnumTabs();

    /**
     * Entspricht der ili2db-Option `--createEnumTxtCol`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateEnumTxtCol();

    /**
     * Entspricht der ili2db-Option `--createEnumColAsItfCode`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateEnumColAsItfCode();

    /**
     * Entspricht der ili2db-Option `--createEnumTabsWithId`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateEnumTabsWithId();

    /**
     * Entspricht der ili2db-Option `--createImportTabs`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateImportTabs();

    /**
     * Entspricht der ili2db-Option `--beautifyEnumDispName`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getBeautifyEnumDispName();

    /**
     * Entspricht der ili2db-Option `--noSmartMapping`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getNoSmartMapping();

    /**
     * Entspricht der ili2db-Option `--smart1Inheritance`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSmart1Inheritance();

    /**
     * Entspricht der ili2db-Option `--smart2Inheritance`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSmart2Inheritance();

    /**
     * Entspricht der ili2db-Option `--coalesceCatalogueRef`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceCatalogueRef();

    /**
     * Entspricht der ili2db-Option `--coalesceMultiSurface`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceMultiSurface();

    /**
     * Entspricht der ili2db-Option `--coalesceMultiline`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceMultiLine();

    /**
     * Entspricht der ili2db-Option `--expandMultilingual`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getExpandMultilingual();

    /**
     * Entspricht der ili2db-Option `--coalesceJson`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceJson();

    /**
     * Entspricht der ili2db-Option `--coalesceArray`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCoalesceArray();

    /**
     * Entspricht der ili2db-Option `--createTypeConstraint`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateTypeConstraint();

    /**
     * Entspricht der ili2db-Option `--createFk`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateFk();

    /**
     * Entspricht der ili2db-Option `--createFkIdx`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateFkIdx();

    /**
     * Entspricht der ili2db-Option `--createUnique`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateUnique();

    /**
     * Entspricht der ili2db-Option `--createNumChecks`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateNumChecks();

    /**
     * Entspricht der ili2db-Option `--createTextChecks`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateTextChecks();

    /**
     * Entspricht der ili2db-Option `--createDateTimeChecks`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateDateTimeChecks();

    /**
     * Entspricht der ili2db-Option `--createStdCols`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateStdCols();
    
    /**
     * Entspricht der ili2db-Option `--t_id_Name`.
     */
    @Input
    @Optional
    public abstract Property<String> getT_id_Name();
    
    /**
     * Entspricht der ili2db-Option `--idSeqMin`.
     */
    @Input
    @Optional
    public abstract Property<Long> getIdSeqMin();
    
    /**
     * Entspricht der ili2db-Option `--idSeqMax`.
     */
    @Input
    @Optional
    public abstract Property<Long> getIdSeqMax();
    
    /**
     * Entspricht der ili2db-Option `--createTypeDescriminator`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateTypeDiscriminator();
    
    /**
     * Entspricht der ili2db-Option `--createGeomIdx`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateGeomIdx();
    
    /**
     * Entspricht der ili2db-Option `--disableNameOptimization`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDisableNameOptimization();
    
    /**
     * Entspricht der ili2db-Option `--nameByTopic`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getNameByTopic();
    
    /**
     * Entspricht der ili2db-Option `--maxNameLength`.
     */
    @Input
    @Optional
    public abstract Property<Integer> getMaxNameLength();
    
    /**
     * Entspricht der ili2db-Option `--sqlEnableNull`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSqlEnableNull();
    
    /**
     * Entspricht der ili2db-Option `--sqlColsAsText`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSqlColsAsText();
    
    /**
     * Entspricht der ili2db-Option `--sqlExtRefCols`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getSqlExtRefCols();
    
    /**
     * Entspricht der ili2db-Option `--keepAreaRef`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getKeepAreaRef();
    
    /**
     * Entspricht der ili2db-Option `--createTidCol`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateTidCol();

    /**
     * Entspricht der ili2db-Option `--createBasketCol`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateBasketCol();

    /**
     * Entspricht der ili2db-Option `--createDatasetCol`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateDatasetCol();

    /**
     * Entspricht der ili2db-Option `--translation`.
     */
    @Input
    @Optional
    public abstract Property<String> getTranslation();

    /**
     * Entspricht der ili2db-Option `--createMetaInfo`.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getCreateMetaInfo();

    protected void init(Config settings) {
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
