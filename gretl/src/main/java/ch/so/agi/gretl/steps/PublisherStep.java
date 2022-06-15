package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.interlis2.validator.Validator;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.ehi.basics.settings.Settings;
import ch.ehi.basics.view.GenericFileFilter;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.fromili.TransferFromIli;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgCustomStrategy;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.iox.IoxException;
import ch.interlis.models.DM01AVCH24LV95D_;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.Grooming;
import ch.so.agi.gretl.util.SimiSvcApi;

public class PublisherStep {
    public static final String FILE_EXT_ZIP = "zip";
    public static final String PATH_ELE_HISTORY = "hist";
    public static final String PATH_ELE_AKTUELL = "aktuell";
    public static final String PATH_ELE_META = "meta";
    public static final String PATH_ELE_PUBLISHDATE_JSON="publishdate.json";
    public static final String PATH_ELE_LEAFLET_HTML="datenbeschreibung.html";
    public static final String JSON_ATTR_PUBLISHDATE="publishdate";
    private static final Object MODEL_DM01 = DM01AVCH24LV95D_.MODEL;
    private GretlLogger log;

    public PublisherStep() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    public void publishDatasetFromDb(Date date,String dataIdent, java.sql.Connection conn, String dbSchema,String datasetName,String exportModels,boolean userFormats, Path target, String regionRegEx,List<String> regionsToPublish,List<String> publishedRegions, Path validationConfig,Path groomingJson,Settings settings,Path tempFolder,SimiSvcApi simiSvc) 
            throws Exception 
    {
        ch.ehi.ili2db.gui.Config config=cloneSettings(new ch.ehi.ili2db.gui.Config(),settings);
        new ch.ehi.ili2pg.PgMain().initConfig(config);
        config.setDbschema(dbSchema);
        config.setExportModels(exportModels);
        String dateTag=getDateTag(date);
        Grooming grooming=null;
        if(groomingJson!=null) {
            grooming=readGrooming(groomingJson);
        }
        if((regionRegEx!=null || regionsToPublish!=null) && datasetName==null){
            if(regionRegEx!=null) {
                log.info("regionRegEx <"+regionRegEx+">");
            }else {
                log.info("regionsToPublish "+regionsToPublish);
            }
            boolean isDM01=false;
            List<String> regions=getRegionsFromDb(conn, config,regionRegEx);
            filterRegions(regions,regionsToPublish);
            if(regions.size()>0){
                ch.interlis.ili2c.config.Configuration modelv=new ch.interlis.ili2c.config.Configuration();
                String dtName = regions.get(0);
                Long datasetId=Ili2db.getDatasetId(dtName, conn, config);
                if(datasetId==null){
                    throw new Ili2dbException("dataset <"+dtName+"> doesn't exist");
                }
                Ili2db.getBasketSqlIdsFromDatasetId(datasetId,modelv,conn,config);
                ch.interlis.ilirepository.IliFiles iliFiles=TransferFromIli.readIliFiles(conn,config.getDbschema(),new PgCustomStrategy(),config.isVer3_export());
                ch.interlis.ili2c.modelscan.IliFile iliFile=iliFiles.iteratorFile().next();
                config.setItfTransferfile(iliFile.getIliVersion()<2.0);
                isDM01=((ch.interlis.ili2c.modelscan.IliModel)(iliFile.iteratorModel().next())).getName().equals(MODEL_DM01);
            }
            publishFilePre(dateTag,dataIdent,target,settings,tempFolder,regions);
            List<Path> modelFiles=null;
            for(String region:regions) {
                String filename=region+"."+dataIdent;
                Path xtfFile=tempFolder.resolve(filename+(config.isItfTransferfile()?".itf":".xtf"));
                Path validationLog=tempFolder.resolve(filename+".log");
                Path gpkgFile=tempFolder.resolve(filename+".gpkg");
                Path shpFolder=tempFolder.resolve(filename+".shp");
                Path dxfFolder=tempFolder.resolve(filename+".dxf");
                try {
                    config.setXtffile(xtfFile.toString());
                    config.setModeldir(Ili2db.ILI_FROM_DB);
                    config.setFunction(Config.FC_EXPORT);
                    config.setDatasetName(region);
                    config.setValidation(false);
                    config.setJdbcConnection(conn);
                    Ili2db.readSettingsFromDb(config);
                    settings.setValue(Config.PREFIX+".defaultSrsAuthority",config.getDefaultSrsAuthority());
                    settings.setValue(Config.PREFIX+".defaultSrsCode",config.getDefaultSrsCode());
                    Ili2db.run(config, null);
                    modelFiles=new ArrayList<Path>();
                    publishFile(dateTag,dataIdent,xtfFile,target,region,validationLog,validationConfig,settings,tempFolder,modelFiles);
                    if(userFormats) {
                        writeGpkgFile(xtfFile,gpkgFile,settings,tempFolder);
                        publishUserFormatFile(dateTag,dataIdent,gpkgFile,target,region,validationLog,validationConfig,settings,tempFolder);
                        writeShpFile(gpkgFile,shpFolder,settings,tempFolder);
                        publishUserFormatFolder(dateTag,dataIdent,shpFolder,target,region,validationLog,validationConfig,settings,tempFolder);
                        if(isDM01) {
                            writeGeobauDxfFile(xtfFile,dxfFolder,settings,tempFolder);
                            publishUserFormatFile(dateTag,dataIdent,dxfFolder,target,region,validationLog,validationConfig,settings,tempFolder);
                        }else {
                            writeDxfFile(gpkgFile,dxfFolder,settings,tempFolder);
                            publishUserFormatFolder(dateTag,dataIdent,dxfFolder,target,region,validationLog,validationConfig,settings,tempFolder);
                        }
                    }
                    publishedRegions.add(region);
                }finally {
                    deleteFile(xtfFile);
                    deleteFile(gpkgFile);
                    deleteFile(validationLog);
                    deleteFileTree(shpFolder);
                    deleteFileTree(dxfFolder);
                }
            }
            publishFilePost(date,dataIdent,target,settings,tempFolder,modelFiles,regions,simiSvc,grooming);
        }else if(datasetName!=null && regionRegEx==null && regionsToPublish==null){
            boolean isDM01=false;
            {
                ch.interlis.ili2c.config.Configuration modelv=new ch.interlis.ili2c.config.Configuration();
                String datasetNames[] = datasetName.split(ch.interlis.ili2c.Main.MODELS_SEPARATOR);
                for (String dtName : datasetNames) {
                    Long datasetId=Ili2db.getDatasetId(dtName, conn, config);
                    if(datasetId==null){
                        throw new Ili2dbException("dataset <"+dtName+"> doesn't exist");
                    }
                    Ili2db.getBasketSqlIdsFromDatasetId(datasetId,modelv,conn,config);
                }
                ch.interlis.ilirepository.IliFiles iliFiles=TransferFromIli.readIliFiles(conn,config.getDbschema(),new PgCustomStrategy(),config.isVer3_export());
                ch.interlis.ili2c.modelscan.IliFile iliFile=iliFiles.iteratorFile().next();
                config.setItfTransferfile(iliFile.getIliVersion()<2.0);
                isDM01=((ch.interlis.ili2c.modelscan.IliModel)(iliFile.iteratorModel().next())).getName().equals(MODEL_DM01);
            }
            String filename=datasetName;
            Path xtfFile=tempFolder.resolve(filename+(config.isItfTransferfile()?".itf":".xtf"));
            Path validationLog=tempFolder.resolve(filename+".log");
            Path gpkgFile=tempFolder.resolve(filename+".gpkg");
            Path shpFolder=tempFolder.resolve(filename+".shp");
            Path dxfFolder=tempFolder.resolve(filename+".dxf");
            try {
                config.setXtffile(xtfFile.toString());
                config.setModeldir(Ili2db.ILI_FROM_DB);
                config.setFunction(Config.FC_EXPORT);
                config.setDatasetName(datasetName);
                config.setValidation(false);
                config.setJdbcConnection(conn);
                Ili2db.readSettingsFromDb(config);
                settings.setValue(Config.PREFIX+".defaultSrsAuthority",config.getDefaultSrsAuthority());
                settings.setValue(Config.PREFIX+".defaultSrsCode",config.getDefaultSrsCode());
                Ili2db.run(config, null);
                List<Path> modelFiles=new ArrayList<Path>();
                publishFilePre(dateTag,dataIdent,target,settings,tempFolder,null);
                publishFile(dateTag,dataIdent,xtfFile,target,null,validationLog,validationConfig,settings,tempFolder,modelFiles);
                if(userFormats) {
                    writeGpkgFile(xtfFile,gpkgFile,settings,tempFolder);
                    publishUserFormatFile(dateTag,dataIdent,gpkgFile,target,null,validationLog,validationConfig,settings,tempFolder);
                    writeShpFile(gpkgFile,shpFolder,settings,tempFolder);
                    publishUserFormatFolder(dateTag,dataIdent,shpFolder,target,null,validationLog,validationConfig,settings,tempFolder);
                    if(isDM01) {
                        writeGeobauDxfFile(xtfFile,dxfFolder,settings,tempFolder);
                        publishUserFormatFile(dateTag,dataIdent,dxfFolder,target,null,validationLog,validationConfig,settings,tempFolder);
                    }else {
                        writeDxfFile(gpkgFile,dxfFolder,settings,tempFolder);
                        publishUserFormatFolder(dateTag,dataIdent,dxfFolder,target,null,validationLog,validationConfig,settings,tempFolder);
                    }
                }
                publishFilePost(date,dataIdent,target,settings,tempFolder,modelFiles,null,simiSvc,grooming);
            }finally {
                deleteFile(xtfFile);
                deleteFile(gpkgFile);
                deleteFile(validationLog);
                deleteFileTree(shpFolder);
                deleteFileTree(dxfFolder);
            }
        }else{
            throw new IllegalArgumentException("regionRegEx==null && regionsToPublish==null && datasetName==null ");
        }
    }
    private static void filterRegions(List<String> regions, List<String> regionsToPublish) {
        if(regionsToPublish!=null) {
            regions.retainAll(regionsToPublish);
        }
    }
    private void deleteFile(Path file) throws IOException {
        try {
            Files.delete(file);
        }catch(java.nio.file.NoSuchFileException e) {
            // ignore
        }
    }
    private void writeDxfFile(Path gpkgFile, Path dxfFolder, Settings settings, Path tempFolder) throws Exception {
        Files.createDirectories(dxfFolder);
        Gpkg2DxfStep converter=new Gpkg2DxfStep();
        converter.execute(gpkgFile.toString(),dxfFolder.toString());
    }
    private void writeGeobauDxfFile(Path itfFile, Path dxfFile, Settings settings, Path tempFolder) throws Exception {
        boolean ok = org.interlis2.av2geobau.Av2geobau.convert(itfFile.toFile(),dxfFile.toFile(),settings);
        if(!ok) {
            throw new Exception("Av2geobau failed");
        }
    }
    private void writeShpFile(Path gpkgFile, Path shpFolder, Settings settings, Path tempFolder) throws IoxException, IOException {
        Files.createDirectories(shpFolder);
        Gpkg2ShpStep converter=new Gpkg2ShpStep();
        converter.execute(gpkgFile.toString(),shpFolder.toString());
    }
    private Config cloneSettings(Config config, Settings settings) {
        if(settings!=null){
            java.util.Iterator<String> it=settings.getValuesIterator();
            while(it.hasNext()){
                String name=it.next();
                String obj=settings.getValue(name);
                config.setValue(name,obj);
            }
            it=settings.getTransientValues().iterator();
            while(it.hasNext()){
                String name=(String)it.next();
                Object obj=settings.getTransientObject(name);
                config.setTransientObject(name,obj);
            }
        }
        return config;
    }
    private void publishUserFormatFile(String date, String dataIdent, Path sourcePath, Path target, String region,Path validationLog,Path validationConfig,Settings settings,
            Path tempFolder) throws IOException {
        Path targetRootPath=target;
        Path targetPath=targetRootPath.resolve(dataIdent);
        String regionPrefix="";
        if(region!=null) {
            regionPrefix=region+".";
        }
        Path targetDatePath=targetPath.resolve("."+date);
        // xtf in zip kopieren
        ZipOutputStream out=null;
        try{
            String sourceName=sourcePath.getFileName().toString();
            String sourceExt="."+GenericFileFilter.getFileExtension(sourceName);
            Path targetFile = targetDatePath.resolve(regionPrefix+dataIdent+sourceExt+"."+FILE_EXT_ZIP);
            out = new ZipOutputStream(Files.newOutputStream(targetFile));
            copyFileToZip(out,regionPrefix+dataIdent+sourceExt,sourcePath);
            copyFileToZip(out,"validation.log",validationLog);
            if(validationConfig!=null) {
                copyFileToZip(out,"validation.ini",validationConfig);
            }
        }finally {
            if(out!=null){
                try {
                    out.closeEntry();
                    out.close();        
                } catch (IOException e) {
                    log.error("failed to close file",e);
                }
                out=null;
            }
        }
    }
    private void publishUserFormatFolder(String date, String dataIdent, Path sourceFolder, Path target, String region,Path validationLog,Path validationConfig,Settings settings,
            Path tempFolder) throws IOException {
        Path targetRootPath=target;
        Path targetPath=targetRootPath.resolve(dataIdent);
        String regionPrefix="";
        if(region!=null) {
            regionPrefix=region+".";
        }
        Path targetDatePath=targetPath.resolve("."+date);
        // xtf in zip kopieren
        ZipOutputStream out=null;
        try{
            String sourceName=sourceFolder.getFileName().toString();
            String sourceExt="."+GenericFileFilter.getFileExtension(sourceName);
            Path targetFile = targetDatePath.resolve(regionPrefix+dataIdent+sourceExt+"."+FILE_EXT_ZIP);
            out = new ZipOutputStream(Files.newOutputStream(targetFile));
            // copy all files from sourceFolder to zip
            copyFilesFromFolderToZip(out, sourceFolder);
            copyFileToZip(out,"validation.log",validationLog);
            if(validationConfig!=null) {
                copyFileToZip(out,"validation.ini",validationConfig);
            }
        }finally {
            if(out!=null){
                try {
                    out.closeEntry();
                    out.close();        
                } catch (IOException e) {
                    log.error("failed to close file",e);
                }
                out=null;
            }
        }
    }
    private void writeGpkgFile(Path xtfFile, Path gpkgFile, Settings settings, Path tempFolder) throws Ili2dbException {
        ch.ehi.ili2db.gui.Config config=cloneSettings(new ch.ehi.ili2db.gui.Config(),settings);
        ch.ehi.ili2gpkg.GpkgMain gpkgMain=new ch.ehi.ili2gpkg.GpkgMain();
        gpkgMain.initConfig(config);
        
        config.setXtffile(xtfFile.toString());
        config.setDbfile(gpkgFile.toString());
        //config.setDburl(gpkgMain.getDbUrlConverter().makeUrl(config));
        config.setDburl("jdbc:sqlite:"+config.getDbfile());
        config.setModeldir(settings.getValue(Validator.SETTING_ILIDIRS));
        config.setFunction(Config.FC_IMPORT);
        config.setDoImplicitSchemaImport(true);
        config.setValidation(false);
        config.setCreateMetaInfo(true); // required by following Gpkg2DxfStep
        Ili2db.run(config, null);
    }
    private List<String> getRegionsFromDb(Connection conn, Config config, String regionRegEx) throws Exception {
        List<String> datasets=Ili2db.getDatasets(conn, config);
        List<String> regions=new ArrayList<String>();
        for(String dataset:datasets) {
            if(regionRegEx==null || dataset.matches(regionRegEx)) {
                regions.add(dataset);
            }
        }
        return regions;
    }
    public void publishDatasetFromFile(Date date,String dataIdent, Path sourcePath, Path target, String regionRegEx,List<String> regionsToPublish,List<String> publishedRegions,Path validationConfig,Path groomingJson,Settings settings,Path tempFolder,SimiSvcApi simiSvc) 
            throws Exception 
    {
        String dateTag=getDateTag(date);
        Grooming grooming=null;
        if(groomingJson!=null) {
            grooming=readGrooming(groomingJson);
        }
        if(regionRegEx!=null || regionsToPublish!=null) {
            if(regionRegEx!=null) {
                log.info("regionRegEx <"+regionRegEx+">");
            }else {
                log.info("regionsToPublish "+regionsToPublish);
            }
            String sourceName=sourcePath.getFileName().toString();
            String sourceExt=GenericFileFilter.getFileExtension(sourceName);
            Path sourceParent=sourcePath.getParent();
            List<String> regions=listRegions(sourceParent, regionRegEx, sourceExt);
            filterRegions(regions,regionsToPublish);
            publishFilePre(dateTag,dataIdent,target,settings,tempFolder,regions);
            List<Path> modelFiles=new ArrayList<Path>();
            for(String region:regions) {
                Path xtfFile=sourceParent.resolve(region+"."+sourceExt);
                Path validationLog=tempFolder.resolve(region+"."+dataIdent+".log");
                modelFiles=new ArrayList<Path>();
                publishFile(dateTag,dataIdent,xtfFile,target,region,validationLog,validationConfig,settings,tempFolder,modelFiles);
                if(publishedRegions!=null) {
                    publishedRegions.add(region);
                }
                Files.delete(validationLog);
            }
            publishFilePost(date,dataIdent,target,settings,tempFolder,modelFiles,regions,simiSvc,grooming);
        }else {
            publishFilePre(dateTag,dataIdent,target,settings,tempFolder,null);
            List<Path> modelFiles=new ArrayList<Path>();
            Path validationLog=tempFolder.resolve(dataIdent+".log");
            publishFile(dateTag,dataIdent,sourcePath,target,null,validationLog,validationConfig,settings,tempFolder,modelFiles);
            publishFilePost(date,dataIdent,target,settings,tempFolder,modelFiles,null,simiSvc,grooming);
            Files.delete(validationLog);
        }
    }
    private void publishFile(String date,String dataIdent, Path sourcePath, Path target, String region,Path logFile,Path validationConfig,Settings settings,Path tempFolder,List<Path> modelFiles) 
            throws Exception 
    {
        String files[]=new String[1];
        files[0]=sourcePath.toAbsolutePath().toString();
        settings.setValue(Validator.SETTING_LOGFILE, logFile.toString());
        if(validationConfig!=null) {
            settings.setValue(Validator.SETTING_CONFIGFILE, validationConfig.toString());
        }
        Validator validator=new Validator();
        boolean ok = validator.validate(files, settings);
        if(!ok) {
            throw new Exception("validation failed");
        }
        Path targetRootPath=target;
        Path targetPath=targetRootPath.resolve(dataIdent);
        String regionPrefix="";
        if(region!=null) {
            regionPrefix=region+".";
        }
        Path targetDatePath=targetPath.resolve("."+date);
        // xtf in zip kopieren
        ZipOutputStream out=null;
        try{
            String sourceName=sourcePath.getFileName().toString();
            String sourceExt="."+GenericFileFilter.getFileExtension(sourceName);
            Path targetFile = targetDatePath.resolve(regionPrefix+dataIdent+sourceExt+"."+FILE_EXT_ZIP);
            out = new ZipOutputStream(Files.newOutputStream(targetFile));
            copyFileToZip(out,regionPrefix+dataIdent+sourceExt,sourcePath);
            copyFileToZip(out,"validation.log",logFile);
            if(validationConfig!=null) {
                copyFileToZip(out,"validation.ini",validationConfig);
            }
            // ilis merken
            Iterator<Model> modeli=validator.getModel().iterator();
            Set<String> visitedFiles=new HashSet<String>();
            while(modeli.hasNext()) {
                Model model=modeli.next();
                if(model instanceof ch.interlis.ili2c.metamodel.PredefinedModel) {
                    continue;
                }
                String filename=model.getFileName();
                if(!visitedFiles.contains(filename)) {
                    modelFiles.add(Paths.get(filename));
                    visitedFiles.add(filename);
                }
            }
        }finally {
            if(out!=null){
                try {
                    out.closeEntry();
                    out.close();        
                } catch (IOException e) {
                    log.error("failed to close file",e);
                }
                out=null;
            }
        }
    }
    private void publishFilePre(String date,String dataIdent, Path target, Settings settings,Path tempFolder,List<String>  regions) 
            throws Exception 
    {
        Path targetRootPath=target;
        // {dataIdent} erzeugen
        Path targetPath=targetRootPath.resolve(dataIdent);
        Files.createDirectories(targetPath);
        Path targetCurrentPath=targetPath.resolve(PATH_ELE_AKTUELL);
        Path targetHistPath=targetPath.resolve(PATH_ELE_HISTORY);
        String currentPublishdate=null;
        if(Files.exists(targetCurrentPath)) {
            currentPublishdate=readPublishDate(targetCurrentPath);
        }
        if(currentPublishdate!=null && currentPublishdate.equals(date)) {
            // Stand im "aktuell" ist der selbe wie nun geschrieben werden soll
            log.info("neuer Stand ("+date+") existiert schon im \"aktuell\" ("+currentPublishdate+") und wird ueberschrieben");
        }
        if(date!=null && Files.exists(targetHistPath.resolve(date))) {
            // neuer Stand existiert auch schon als History
            throw new IllegalArgumentException("neuer Stand ("+date+") existiert auch schon als History");
        }
        if(currentPublishdate!=null && Files.exists(targetHistPath.resolve(currentPublishdate))) {
            // Stand im "aktuell" existiert auch schon als History
            throw new IllegalArgumentException("Stand im \"aktuell\" ("+currentPublishdate+") existiert auch schon als History");
        }
        // .{date} erzeugen
        Path targetDatePath=targetPath.resolve("."+date);
        Files.createDirectories(targetDatePath);
        // regionen update
        if(regions!=null) {
            // Verzeichnis "aktuell" existiert?
            if(currentPublishdate!=null) {
                // Daten aus "aktuell" in neues Zielverzeichnis kopieren
                for(Path file:Files.newDirectoryStream(targetCurrentPath)) {
                    Path zipFilename=file.getFileName();
                    if(FILE_EXT_ZIP.equals(GenericFileFilter.getFileExtension(zipFilename.toString()))) {
                        Files.copy(file, targetDatePath.resolve(zipFilename));
                    }
                }
            }
        }
    }
    private void publishFilePost(Date date,String dataIdent, Path target, Settings settings,Path tempFolder,List<Path> modelFiles,List<String> publishedRegions,SimiSvcApi simiSvc,Grooming grooming) 
            throws Exception 
    {
        Path targetRootPath=target;
        Path targetPath=targetRootPath.resolve(dataIdent);
        Path targetCurrentPath=targetPath.resolve(PATH_ELE_AKTUELL);
        Path targetHistPath=targetPath.resolve(PATH_ELE_HISTORY);
        String currentPublishdate=null;
        if(Files.exists(targetCurrentPath)) {
            currentPublishdate=readPublishDate(targetCurrentPath);
        }
        String dateTag=getDateTag(date);
        Path targetDatePath=targetPath.resolve("."+dateTag);
        // meta erzeugen
        Path targetMetaPath=targetDatePath.resolve(PATH_ELE_META);
        Files.createDirectories(targetMetaPath);
        // ilis kopieren
        for(Path modelFile:modelFiles) {
           Files.copy(modelFile, targetMetaPath.resolve(modelFile.getFileName().toString()));
        }
        // publishdate.json erzeugen
        writePublishDate(targetDatePath,dateTag);
        // Beipackzettel erzeugen
        if(simiSvc!=null) {
            String leaflet=simiSvc.getLeaflet(dataIdent,date);
            Path leafletPath=getLeafletPath(targetDatePath);
            Files.write(leafletPath, leaflet.getBytes(StandardCharsets.UTF_8));
        }
        // aktuell umbenennen auf Ordnername gemaess Datum in publishdate.json
        if(Files.exists(targetCurrentPath)) {
            if(currentPublishdate!=null) {
                if(currentPublishdate.equals(dateTag)) {
                    deleteFileTree(targetCurrentPath);
                }else {
                    Files.createDirectories(targetHistPath);
                    Files.move(targetCurrentPath,targetHistPath.resolve(currentPublishdate));
                }
            }
        }
        // .{date} in aktuell umbennen
        Files.move(targetDatePath,targetCurrentPath);
        
        // Publikationsdatum in KGDI-Metadaten nachfuehren
        if(simiSvc!=null) {
            simiSvc.notifyPublication(dataIdent, date, publishedRegions);
        }
        // ausduennen
        if(grooming!=null) {
            // get list of folders from targetHistPath
            List<Date> allHistory=listHistory(targetHistPath);
            // get list of folders to delete
            List<Date> deleteDates=new ArrayList<Date>();
            grooming.getFilesToDelete(date,allHistory,deleteDates);
            // delete folders
            for(Date deleteDate:deleteDates) {
                String deleteName=getDateTag(deleteDate);
                Path folderToDelete=targetHistPath.resolve(deleteName);
                try {
                    deleteFileTree(folderToDelete);
                }catch(IOException ex) {
                    log.error("failed to delete history folder "+deleteName,ex);
                }
            }
        }
    }
    public static String getDateTag(Date date) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(date);
    }
    public static Date parseDateTag(String value) throws ParseException {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(value);
    }
    private void copyFilesFromFolderToZip(ZipOutputStream out,Path sourceFolder) throws IOException {
        //ZipOutputStream out=null;
        Files.walkFileTree(sourceFolder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
                if (!Files.isDirectory(file)) {
                    String fileName=file.getFileName().toString();
                    copyFileToZip(out,fileName,file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    private void copyFileToZip(ZipOutputStream out, String filename, Path sourcePath) throws IOException {
        java.io.BufferedInputStream in=null;
        try {
            in=new java.io.BufferedInputStream(Files.newInputStream(sourcePath));
            ZipEntry e = new ZipEntry(filename);
            out.putNextEntry(e);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = in.read(buf)) != -1) {
                out.write(buf, 0, i);
            }
        }finally {
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("failed to close file",e);
                }
                in=null;
            }
        }
    }
    public static void deleteFileTree(Path pathToBeDeleted) throws IOException {
        if(Files.exists(pathToBeDeleted)){
            Files.walkFileTree(pathToBeDeleted, 
                    new SimpleFileVisitor<Path>() {
                      @Override
                      public FileVisitResult postVisitDirectory(
                        Path dir, IOException exc) throws IOException {
                          Files.delete(dir);
                          return FileVisitResult.CONTINUE;
                      }
                      
                      @Override
                      public FileVisitResult visitFile(
                        Path file, BasicFileAttributes attrs) 
                        throws IOException {
                          Files.delete(file);
                          return FileVisitResult.CONTINUE;
                      }
                  });
        }
    }
    public static List<String> listRegions(Path dir,String pattern,String ext) throws IOException {
        List<String> fileList = new ArrayList<String>();
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
                if (!Files.isDirectory(file)) {
                    String fileName=file.getFileName().toString();
                    if(pattern==null) {
                        String fileExt=GenericFileFilter.getFileExtension(fileName);
                        if(ext.equals(fileExt)) {
                            fileList.add(fileName.substring(0,fileName.length()-ext.length()-1));
                        }
                    }else {
                        if(fileName.matches(pattern+"\\."+ext)) {
                            fileList.add(fileName.substring(0,fileName.length()-ext.length()-1));
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return fileList;
    }    
    public static List<Date> listHistory(Path dir) throws IOException {
        List<Date> fileList = new ArrayList<Date>();
        Path ret=Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs)
              throws IOException {
                String fileName=file.getFileName().toString();
                if(fileName.equals(PATH_ELE_HISTORY)) {
                    return FileVisitResult.CONTINUE;
                }
                if(fileName.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
                    try {
                        Date date=parseDateTag(fileName);
                        fileList.add(date);
                    } catch (ParseException e) {
                        // ignore folder
                    }
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
        });
        return fileList;
    }    
    public static Grooming readGrooming(Path groomingJson) throws IOException {
        if(!Files.exists(groomingJson)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        Grooming grooming = mapper.readValue(Files.newInputStream(groomingJson), Grooming.class);
        grooming.isValid();
        return grooming;
    }
    public static void writePublishDate(Path targetPath, String date) throws IOException {
        Path publishdatePath = getPublishdatePath(targetPath);
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put(JSON_ATTR_PUBLISHDATE, date);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(Files.newOutputStream(publishdatePath), map);
    }
    public static String readPublishDate(Path targetPath) throws IOException {
        Path publishdatePath = getPublishdatePath(targetPath);
        if(!Files.exists(publishdatePath)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map map = mapper.readValue(Files.newInputStream(publishdatePath), java.util.Map.class);
        String date=(String)map.get(JSON_ATTR_PUBLISHDATE);
        return date;
    }
    private static Path getPublishdatePath(Path targetPath) {
        Path targetMetaPath=targetPath.resolve(PATH_ELE_META);
        Path publishdatePath=targetMetaPath.resolve(PATH_ELE_PUBLISHDATE_JSON);
        return publishdatePath;
    }
    private static Path getLeafletPath(Path targetPath) {
        Path targetMetaPath=targetPath.resolve(PATH_ELE_META);
        Path leafletPath=targetMetaPath.resolve(PATH_ELE_LEAFLET_HTML);
        return leafletPath;
    }
}
