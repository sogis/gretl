package ch.so.agi.gretl.util;

import java.io.File;

/**
 * Utility-Class to get File extensions
 */
public class FileExtension {

    private FileExtension() {
    }

    /**
     * Gets the extension of the given file.
     *
     * @param inputFile File, which should be checked for the extension
     * @return          file extension (e.g. ".sql")
     * @throws GretlException if no extension
     */
    public static String getFileExtension(File inputFile)
            throws Exception {

        String [] splittedFilePath = splitFilePathAtPoint(inputFile);
        return getFileExtensionFromArray(splittedFilePath);

    }

    private static String[] splitFilePathAtPoint (File inputFile)
            throws Exception {
        String filePath =inputFile.getAbsolutePath();
        return filePath.split("\\.");
    }

    private static String getFileExtensionFromArray(String[] splittedFilePath) throws Exception{
        Integer arrayLength=splittedFilePath.length;
        if (arrayLength >= 2) {
            return splittedFilePath[arrayLength - 1];
        } else  {
            throw new GretlException(GretlException.TYPE_MISSING_FILE_EXTENSION, "File must have a file extension");
        }
    }
}



