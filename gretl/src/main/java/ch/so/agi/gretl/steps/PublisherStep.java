package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.interlis2.validator.Validator;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class PublisherStep {
    public static final String PATH_ELE_HISTORY = "hist";
    public static final String PATH_ELE_AKTUELL = "aktuell";
    public static final String PATH_ELE_META = "meta";
    public static final String PATH_ELE_PUBLISHDATE_JSON="publishdate.json";
    public static final String JSON_ATTR_PUBLISHDATE="publishdate";
    private GretlLogger log;

    public PublisherStep() {
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    public void publishFromFile(String date,String dataIdent, Path sourcePath, Path target, String targetUsr, String targetPwd,String regionRegEx,List<String> publishedRegions,boolean validate,File groomingJson,Settings settings) 
            throws Exception 
    {
        String files[]=new String[1];
        files[0]=sourcePath.toAbsolutePath().toString();
        Validator validator=new Validator();
        boolean ok = validator.validate(files, settings);
        if(!ok) {
            throw new Exception("validation failed");
        }
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
        // xtf in zip kopieren
        java.io.BufferedInputStream in=null;
        ZipOutputStream out=null;
        try{
            try {
                in=new java.io.BufferedInputStream(Files.newInputStream(sourcePath));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("failed to open file",e);
            }
            String sourceName=sourcePath.getFileName().toString();
            Path targetFile = targetDatePath.resolve(sourceName+".zip");
            out = new ZipOutputStream(Files.newOutputStream(targetFile));
            ZipEntry e = new ZipEntry(sourceName);
            out.putNextEntry(e);

            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = in.read(buf)) != -1) {
                    out.write(buf, 0, i);
                }
            } catch (IOException ex) {
                throw new IllegalArgumentException("failed to copy file",ex);
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
            if(out!=null){
                try {
                    out.closeEntry();
                    out.close();        
                } catch (IOException e) {
                    log.error("failed to colse file",e);
                }
                out=null;
            }
        }
        // meta erzeugen
        Path targetMetaPath=targetDatePath.resolve(PATH_ELE_META);
        Files.createDirectories(targetMetaPath);
        // ilis kopieren
        Iterator<Model> modeli=validator.getModel().iterator();
        Set<String> visitedFiles=new HashSet<String>();
        while(modeli.hasNext()) {
            Model model=modeli.next();
            if(model instanceof ch.interlis.ili2c.metamodel.PredefinedModel) {
                continue;
            }
            String filename=model.getFileName();
            if(!visitedFiles.contains(filename)) {
                Files.copy(Paths.get(filename), targetMetaPath.resolve(Paths.get(filename).getFileName().toString()));
                visitedFiles.add(filename);
            }
        }
        // publishdate.json erzeugen
        writePublishDate(targetDatePath,date);
        // aktuell umbenennen auf Ordnername gemaess Datum in publishdate.json
        if(Files.exists(targetCurrentPath)) {
            if(currentPublishdate!=null) {
                if(currentPublishdate.equals(date)) {
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
        // ausduennen
        
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
}
