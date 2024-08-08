package ch.so.agi.gretl.testutil;

import java.net.URI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SftpUriTest {
    @Test
    public void parserTest() throws Exception {
        URI uri = new URI( "sftp://ftp.umleditor.org/publisher" );
        String path=uri.getRawPath();
        assertEquals("/publisher",path);
        URI base= new URI(uri.getScheme()+"://"+uri.getHost());
        assertEquals("sftp://ftp.umleditor.org", base.toString());
    }
    @Test
    public void relativeFileTest() throws Exception{
        URI uri = new URI( "publisher" );
        assertEquals(null, uri.getScheme());
        assertEquals("publisher", uri.getRawPath());
    }
    @Test
    public void absoluteFileTest() throws Exception{
        URI uri = new URI( "/publisher" );
        assertEquals(null, uri.getScheme());
        assertEquals("/publisher", uri.getRawPath());
    }

}
