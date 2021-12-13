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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.interlis2.validator.Validator;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ehi.basics.settings.Settings;
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
    public void publishFromFile(String date,String dataIdent, File sourcePath, String target, String targetUsr, String targetPwd,String regionRegEx,List<String> publishedRegions,boolean validate,File groomingJson,Settings settings) 
            throws Exception 
    {
        String files[]=new String[1];
        files[0]=sourcePath.getAbsolutePath();
        Validator validator=new Validator();
        boolean ok = validator.validate(files, settings);
        if(!ok) {
            throw new Exception("validation failed");
        }
        File targetRootPath=new File(target);
        // {dataIdent} erzeugen
        File targetPath=new File(targetRootPath,dataIdent);
        targetPath.mkdirs();
        File targetCurrentPath=new File(targetPath,PATH_ELE_AKTUELL);
        File targetHistPath=new File(targetPath,PATH_ELE_HISTORY);
        String currentPublishdate=null;
        if(targetCurrentPath.exists()) {
            currentPublishdate=readPublishDate(targetCurrentPath);
        }
        if(currentPublishdate!=null && currentPublishdate.equals(date)) {
            // Stand im "aktuell" ist der selbe wie nun geschrieben werden soll
            log.info("neuer Stand ("+date+") existiert schon im \"aktuell\" ("+currentPublishdate+") und wird ueberschrieben");
        }
        if(date!=null && new File(targetHistPath,date).exists()) {
            // neuer Stand existiert auch schon als History
            throw new IllegalArgumentException("neuer Stand ("+date+") existiert auch schon als History");
        }
        if(currentPublishdate!=null && new File(targetHistPath,currentPublishdate).exists()) {
            // Stand im "aktuell" existiert auch schon als History
            throw new IllegalArgumentException("Stand im \"aktuell\" ("+currentPublishdate+") existiert auch schon als History");
        }
        // .{date} erzeugen
        File targetDatePath=new File(targetPath,"."+date);
        targetDatePath.mkdirs();
        // xtf in zip kopieren
        java.io.BufferedInputStream in=null;
        ZipOutputStream out=null;
        try{
            try {
                in=new java.io.BufferedInputStream(new FileInputStream(sourcePath));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("failed to open file",e);
            }
            String sourceName=sourcePath.getName();
            File targetFile = new File(targetDatePath,sourceName+".zip");
            out = new ZipOutputStream(new FileOutputStream(targetFile));
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
        File targetMetaPath=new File(targetDatePath,PATH_ELE_META);
        targetMetaPath.mkdirs();
        // ilis kopieren
        // publishdate.json erzeugen
        writePublishDate(targetDatePath,date);
        // aktuell umbenennen auf Ordnername gemaess Datum in publishdate.json
        if(targetCurrentPath.exists()) {
            if(currentPublishdate!=null) {
                if(currentPublishdate.equals(date)) {
                    deleteFileTree(targetCurrentPath.toPath());
                }else {
                    Files.move(targetCurrentPath.toPath(),new File(targetHistPath,currentPublishdate).toPath());
                }
            }
        }
        // .{date} in aktuell umbennen
        Files.move(targetDatePath.toPath(),targetCurrentPath.toPath());
        
        // Publikationsdatum in KGDI-Metadaten nachfuehren
        // ausduennen
        
    }
    private void deleteFileTree(Path pathToBeDeleted) throws IOException {
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
    private void writePublishDate(File targetPath, String date) throws IOException {
        File publishdatePath = getPublishdatePath(targetPath);
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put(JSON_ATTR_PUBLISHDATE, date);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(publishdatePath, map);
    }
    private String readPublishDate(File targetPath) throws IOException {
        File publishdatePath = getPublishdatePath(targetPath);
        if(!publishdatePath.exists()) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map map = mapper.readValue(publishdatePath, java.util.Map.class);
        String date=(String)map.get(JSON_ATTR_PUBLISHDATE);
        return date;
    }
    private File getPublishdatePath(File targetPath) {
        File targetMetaPath=new File(targetPath,PATH_ELE_META);
        File publishdatePath=new File(targetMetaPath,PATH_ELE_PUBLISHDATE_JSON);
        return publishdatePath;
    }
}
