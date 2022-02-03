package ch.so.agi.gretl.steps;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.experimental.categories.Category;

import com.github.robtimus.filesystems.sftp.SFTPEnvironment;

import ch.so.agi.gretl.testutil.SftpTest;

@Category(SftpTest.class)
public class PublisherStepRemoteTest extends AbstractPublisherStepTest {
    private static FileSystem fileSystem=null;
    static String ftpurl=System.getProperty("ftpurl"); 
    static String ftpusr=System.getProperty("ftpusr");
    static String ftppwd=System.getProperty("ftppwd"); 
    static String path=null;
    @org.junit.BeforeClass
    static  public void initFileSystem()
    {
        if(fileSystem==null) {
            URI host=null;
            URI rawuri=null;
            try {
                rawuri = new URI( ftpurl);
                path=rawuri.getRawPath();
                if(rawuri.getPort()==-1) {
                    host= new URI(rawuri.getScheme()+"://"+rawuri.getHost());
                }else {
                    host= new URI(rawuri.getScheme()+"://"+rawuri.getHost()+":"+rawuri.getPort());
                }
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
            SFTPEnvironment environment = new SFTPEnvironment()
                    .withUsername(ftpusr)
                    .withPassword(ftppwd.toCharArray())
                    .withKnownHosts(new File(System.getProperty("user.home"),".ssh/known_hosts"));
            try {
                fileSystem = FileSystems.newFileSystem( host, environment );
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
    public PublisherStepRemoteTest() {
        super();
    }
    @Override
    protected Path getTargetPath() {
        
        Path remoteTestOut = fileSystem.getPath(path);
        return remoteTestOut;
    }
}
