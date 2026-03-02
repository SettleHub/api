package org.ossfmct.projects.files;

import org.ossfmct.projects.files.config.StorageConfiguration;
import org.ossfmct.projects.files.interfaces.IFileManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileManager implements IFileManagerService {
    private final Logger logger = LoggerFactory.getLogger(FileManager.class);

    private final Path rootLocation;

    public FileManager(StorageConfiguration StorageConfiguration) {
        this.rootLocation = Paths.get(StorageConfiguration.getLocation());
    }

    private Path resolvePath(String fileName) {
        return rootLocation.resolve(fileName).normalize();
    }

    /**
     * Saves a file to the specified location.
     * If directory in specified location not exists, directory will be created.
     *
     * @param inputStream  the input stream of the file to save.
     * @param storagePath  the path of the file to save.
     * @throws IOException if an I/O error occurs during file saving.
     */
    @Override
    public void saveFile(InputStream inputStream, String storagePath) throws IOException {
        Path filePath = resolvePath(storagePath);
        Files.createDirectories(filePath.getParent());
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Loads a file from the specified location.
     *
     * @param storagePath the directory, delimiter and name of the file to load
     * @return a {@link Resource} representing the loaded file.
     * @throws IOException if an I/O error occurs while loading the file.
     */
    @Override
    public Resource loadFile(String storagePath) throws IOException {
        Path filePath = resolvePath(storagePath);

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            logger.error("File not found: {}", storagePath);
            throw new IOException("File: '" + storagePath + "' was not found.");
        }

        return new UrlResource(filePath.toUri());
    }

    /**
     * Checks if a file exists at the specified location.
     *
     * @param fileName the name of the file to check.
     * @return true if the file exists, false otherwise.
     */
    @Override
    public boolean fileExists(String fileName) {
        Path filePath = resolvePath(fileName);
        return Files.exists(filePath);
    }

    /**
     * Removes a file from the specified location.
     *
     * @param storagePath  the path of the file to remove.
     * @throws IOException if an I/O error occurs while removing the file.
     */
    @Override
    public void removeFile(String storagePath) throws IOException {
        Path filePath = resolvePath(storagePath);
        Files.deleteIfExists(filePath);
    }

    /**
     * Lists all files in the specified location.
     *
     * @param location the location to list the files from.
     * @return a list of file names present in the specified location.
     * @throws IOException if an I/O error occurs while listing the files.
     */
    @Override
    public List<String> listFiles(String location) throws IOException {
        Path dirPath = rootLocation.resolve(location).normalize();
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) return List.of();

        try (Stream<Path> stream = Files.list(dirPath)) {
            return stream
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
        }
    }

    /**
     * Updates an existing file with new content.
     *
     * @param fileName    the name of the file to update
     * @param inputStream the input stream containing the new content to update the file with
     * @throws IOException if an I/O error occurs during the file update
     */
    @Override
    public void updateFile(String fileName, InputStream inputStream) throws IOException {
        saveFile(inputStream, fileName); // просто перезапис
    }

    /**
     * Retrieves metadata for a specific file.
     * Metadata may include details such as size, MIME type, creating and last modified date.
     *
     * @param fileName the name of the file to retrieve metadata for.
     * @return a map containing key-value pairs representing the file's metadata.
     * @throws IOException if an I/O error occurs while retrieving file metadata.
     */
    @Override
    public Map<String, String> getFileMetadata(String fileName) throws IOException {
        Path filePath = resolvePath(fileName);
        if (!Files.exists(filePath)) throw new IOException("File not found");

        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
        String mimeType = Files.probeContentType(filePath);

        return Map.of(
            "size", String.valueOf(attrs.size()),
            "mimeType", mimeType != null ? mimeType : "unknown",
            "createdAt", attrs.creationTime().toString(),
            "modifiedAt", attrs.lastModifiedTime().toString()
        );
    }
}
