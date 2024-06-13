package ch.so.agi.gretl.steps;

import ch.so.agi.gretl.testutil.SftpTest;
import com.github.robtimus.filesystems.sftp.SFTPEnvironment;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@Category(SftpTest.class)
public class PublisherStepFile2RemoteTest extends AbstractPublisherStepTest {
    private static final String FTP_URL = System.getProperty("ftpurl");
    private static final String FTP_USER = System.getProperty("ftpusr");
    private static final String FTP_PASSWORD = System.getProperty("ftppwd");
    private static final String KNOWN_HOSTS_PATH = System.getProperty("user.home") + "/.ssh/known_hosts";

    private static FileSystem fileSystem = null;
    private static String path;

    @BeforeClass
    public static void initFileSystem() {
        if (fileSystem != null) {
            return;
        }

        try {
            URI rawUri = new URI(FTP_URL);
            path = rawUri.getRawPath();
            URI host = createHostURI(rawUri);

            SFTPEnvironment environment = new SFTPEnvironment()
                    .withUsername(FTP_USER)
                    .withPassword(FTP_PASSWORD.toCharArray())
                    .withKnownHosts(new File(KNOWN_HOSTS_PATH));

            fileSystem = FileSystems.newFileSystem(host, environment);
        } catch (URISyntaxException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static URI createHostURI(URI rawUri) throws URISyntaxException {
        String scheme = rawUri.getScheme();
        String host = rawUri.getHost();
        int port = rawUri.getPort();

        if (port == -1) {
            return new URI(String.format("%s://%s", scheme, host));
        } else {
            return new URI(String.format("%s://%s:%d", scheme, host, port));
        }
    }

    @Override
    protected Path getTargetPath() {
        return fileSystem.getPath(path);
    }
}
