package org.ossfmct.projects.files.interfaces;

import org.springframework.core.io.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Interface that defines methods for working with files.
 * Provides operations to save, load, update, remove, check existence, and list files,
 * as well as retrieve file metadata.
 */
public interface IFileManagerService {

    /**
     * Saves a file to the specified location.
     * If directory in specified location not exists, directory will be created.
     *
     * @param inputStream  the input stream of the file to save.
     * @param storagePath  the path of the file to save.
     * @throws IOException if an I/O error occurs during file saving.
     */
    void saveFile(InputStream inputStream, String storagePath) throws IOException;

    /**
     * Loads a file from the specified location.
     *
     * @param storagePath the directory, delimiter and name of the file to load
     * @return a {@link Resource} representing the loaded file.
     * @throws IOException if an I/O error occurs while loading the file.
     */
    Resource loadFile(String storagePath) throws IOException;

    /**
     * Checks if a file exists at the specified location.
     *
     * @param fileName the name of the file to check.
     * @return true if the file exists, false otherwise.
     */
    boolean fileExists(String fileName);

    /**
     * Removes a file from the specified location.
     *
     * @param storagePath  the path of the file to remove.
     * @throws IOException if an I/O error occurs while removing the file.
     */
    void removeFile(String storagePath) throws IOException;

    /**
     * Lists all files in the specified location.
     *
     * @param location the location to list the files from.
     * @return a list of file names present in the specified location.
     * @throws IOException if an I/O error occurs while listing the files.
     */
    List<String> listFiles(String location) throws IOException;

    /**
     * Updates an existing file with new content.
     *
     * @param fileName the name of the file to update
     * @param inputStream the input stream containing the new content to update the file with
     * @throws IOException if an I/O error occurs during the file update
     */
    void updateFile(String fileName, InputStream inputStream) throws IOException;

    /**
     * Retrieves metadata for a specific file.
     * Metadata may include details such as size, MIME type, creating and last modified date.
     *
     * @param fileName the name of the file to retrieve metadata for.
     * @return a map containing key-value pairs representing the file's metadata.
     * @throws IOException if an I/O error occurs while retrieving file metadata.
     */
    Map<String, String> getFileMetadata(String fileName) throws IOException;
}