package org.ossfmct.projects.files.repositories;

import org.ossfmct.projects.files.models.UserFile;
import org.ossfmct.projects.users.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing users personal files
 * members of {@link UserFile} class.
 */
public interface FileRepository extends JpaRepository<UserFile, Long> {

    /**
     * Finds a file by its unique id.
     *
     * @param id the unique identifier of the file
     * @return an Optional containing the file if found, or empty otherwise
     */
    Optional<UserFile> findById(Long id);

    /**
     * Finds all files by specified filename.
     *
     * @param filename name of file, without paths and directories.
     * @return a list of files, list may be empty.
     */
    List<UserFile> findAllByFilename(String filename);

    /**
     * Finds all files created at the given date.
     *
     * @param createdAt the date when the file were created, must be in standard <b>ISO 8061</b>
     * @return a list of files created at the specified date
     */
    List<UserFile> findAllByCreatedAt(String createdAt);


    /**
     * Finds all files of specified storage path.
     *
     * @param storagePath it is file location, consists of userId directory, OS delimiter and filename.
     * @return file if it exists.
     */
    Optional<UserFile> findAllByStoragePath(String storagePath);

    /**
     * Finds all files by their owner.
     *
     * @param owner the service user that upload file
     * @return List of files, list may be empty
     */
    List<UserFile> findAllByOwner(User owner);


    /**
     * Deletes a file by its unique identifier.
     *
     * @param id the unique id of the file to delete
     */
    void deleteById(Long id);
}
