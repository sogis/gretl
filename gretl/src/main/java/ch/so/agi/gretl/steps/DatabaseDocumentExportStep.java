package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

public class DatabaseDocumentExportStep {
    private GretlLogger log;
    private String taskName;
    
    public DatabaseDocumentExportStep() {
        this(null);
    }
    
    public DatabaseDocumentExportStep(String taskName) {
        if (taskName == null) {
            this.taskName = DatabaseDocumentExportStep.class.getSimpleName();
        } else {
            this.taskName = taskName;
        }
        this.log = LogEnvironment.getLogger(this.getClass());
    }
    
    /**
     * Exports documents from a database. The file can either be a blob (TODO) or 
     * an url.
     * Url: It will use the last part of the path as filename. 
     * 
     * @param database Database properties to generate database connection
     * @param qualifiedTableName The qualified table name
     * @param documentColumn The name of the database column where the document or the url is stored
     * @param targetDir The name of the directory where the exported documents will be saved
     * @param fileNamePrefix A prefix for the file name
     * @param fileNameExtension The filename extension
     * @throws Exception if ... 
     */
    public void execute(Connector database, String qualifiedTableName, String documentColumn, String targetDir, String fileNamePrefix, String fileNameExtension) throws Exception {
        log.lifecycle(String.format("Start DatabaseDocumentExport(Name: %s Database: %s QualifiedTableName: %s DocumentColumn: %s TargetDir: %s FileNamePrefix: %s FileNameExtension: %s)", taskName,
                database, qualifiedTableName, documentColumn, targetDir, fileNamePrefix, fileNameExtension));
        
        int exportedDocuments = 0;
 
        // https://stackoverflow.com/questions/2893819/accept-servers-self-signed-ssl-certificate-in-java-client
        // Install the all-trusting trust manager
        TrustManager[] trustAllCerts = getTrustAllCerts(); 
        SSLContext sc = SSLContext.getInstance("SSL"); 
        sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        try (Connection conn = database.connect(); Statement stmt = conn.createStatement()) {
            String sql = "SELECT DISTINCT ON ("+documentColumn+") "+documentColumn+" FROM "+qualifiedTableName+" WHERE "+documentColumn+" IS NOT NULL;";
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while(rs.next()) {
                    String urlString = rs.getString(documentColumn);
                    //log.lifecycle("Url: " + urlString);
                    URL url = new URL(urlString);
                   
                    int fromPos = url.getPath().lastIndexOf("/");
                    String fileName = url.getPath().substring(fromPos+1);
                    
                    if (fileNamePrefix != null) {
                        fileName = fileNamePrefix + fileName; 
                    }
                    
                    if (fileNameExtension != null) {
                        fileName = fileName + "." + fileNameExtension;
                    }
                   
                    HttpURLConnection httpConnection = null;
                    int responseCode = 0;

                    httpConnection = (HttpURLConnection) url.openConnection();
                    httpConnection.setRequestMethod("GET");
                    responseCode = httpConnection.getResponseCode();
                    
                    if (responseCode > 399) {
                        String msg = taskName + ": Status code " + String.valueOf(responseCode) + ". Ignoring file ("+urlString+") and continue.";
                        log.lifecycle(msg);
                        continue;
                    } 
                    
                    File document = Paths.get(targetDir, fileName).toFile();         
                    InputStream initialStream = httpConnection.getInputStream();
                    java.nio.file.Files.copy(initialStream, document.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    initialStream.close();

                    log.debug("Document saved: " + document.getAbsolutePath());
                    
                    exportedDocuments++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        log.lifecycle(taskName + ": " + exportedDocuments + " Documents have been exported.");
    }
    
    // Create a trust manager that does not validate certificate chains
    private TrustManager[] getTrustAllCerts() {
        TrustManager[] trustAllCerts = new TrustManager[] { 
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            } 
        };
        return trustAllCerts;
    }
}
