package ch.so.agi.gretl.util;


import java.io.*;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * The FileStylingDefinition class checks if a given file is encoded in UTF-8 and has no Byte-Order-Mark (BOM)
 */
public class FileStylingDefinition {

    private static final String stringBOM = "\uFEFF";
    private static final String encoding ="UTF-8";

    private FileStylingDefinition() {
    }

    /**
     * Checks the given file if it is encoded in UTF-8
     *
     * @param inputfile     File
     * @throws Exception    if the File is not encoded in UTF-8 an Exception will be thrown
     */
    public static void checkForUtf8(File inputfile)
            throws Exception {

        byte[] buffer = new byte[256];
        int fileEnd =-1;



        FileInputStream sqlFileInputStream = new FileInputStream(inputfile);
        BufferedInputStream bufferedInputFileStream = new BufferedInputStream(sqlFileInputStream);

        CharsetDecoder decoder = createCharsetDecoder();

        int lineBytes = bufferedInputFileStream.read(buffer);
        while (lineBytes != fileEnd) {
            try {
                decoder.decode(ByteBuffer.wrap(buffer));
                lineBytes = bufferedInputFileStream.read(buffer);
            } catch (CharacterCodingException e) {
                throw new GretlException("Wrong encoding (not UTF-8) detected in File " + inputfile.getAbsolutePath());
            }
        }
    }

    /**
     * Creates an CharsetDecoder which tests the encoding
     *
     * @return      CharsetDecoder
     */
    private static CharsetDecoder createCharsetDecoder() {
        Charset charset = Charset.forName(encoding);
        CharsetDecoder decoder = charset.newDecoder();
        decoder.reset();

        return decoder;
    }

    /**
     * Checks if the given file starts with a Byte-Order-Mark (BOM)
     *
     * @param inputfile     File
     * @throws Exception    if the File holds a BOM an Exception will be thrown
     */
    public static void checkForBOMInFile(File inputfile) throws Exception{
        FileInputStream sqlFileInputStream = new FileInputStream(inputfile);
        BufferedReader bufferedInputFileStream = new BufferedReader(new InputStreamReader(sqlFileInputStream, "UTF-8"));

        String line = bufferedInputFileStream.readLine();
        if (line.startsWith(stringBOM)) {
            bufferedInputFileStream.close();
            throw new GretlException(GretlException.TYPE_FILE_WITH_BOM, "File includes not allowed BOM");
        } else {
            bufferedInputFileStream.close();
        }
    }


}

