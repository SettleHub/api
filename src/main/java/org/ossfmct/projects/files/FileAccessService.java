package org.ossfmct.projects.files;

import org.ossfmct.projects.files.interfaces.IFileManagerService;
import org.ossfmct.projects.files.models.UserFile;
import org.ossfmct.projects.files.repositories.FileRepository;
import org.ossfmct.projects.submissions.enums.ServiceOperationStatus;
import org.ossfmct.projects.submissions.models.Submission;
import org.ossfmct.projects.tools.FileMetadataTool;
import org.ossfmct.projects.tools.models.Metadata;
import org.ossfmct.projects.users.models.User;
import org.ossfmct.projects.users.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FileAccessService {
    private final Logger logger = LoggerFactory.getLogger(FileAccessService.class);

    @Autowired
    private IFileManagerService fileManager;

    @Autowired
    private UsersService usersService;

    @Autowired
    private FileRepository fileRepository;

    public Optional<Resource> load(Long file_id, User user) {
        Optional<UserFile> optionalFileMeta = fileRepository.findById(file_id);
        UserFile fileMetaData;
        if (optionalFileMeta.isEmpty()) {
            return Optional.empty();
        }
        fileMetaData = optionalFileMeta.get();
        return loadResource(fileMetaData, user);
    }

    private Optional<Resource> loadResource(UserFile fileMetaData, User user) {
        if (usersService.hasModeratorOrAdminRole(user)
            || fileMetaData.getOwner().getId().equals(user.getId()))
        {
            try {
                Resource resource = fileManager.loadFile(fileMetaData.getStoragePath());
                logger.info("Accessing resource: {}", fileMetaData);
                return Optional.of(resource);
            } catch (IOException e) {
                logger.error("Error to access resource: {}", e.getMessage());
            }
        }
        return Optional.empty();
    }

    private record UserFileOperationResult(MultipartFile file, ServiceOperationStatus status) { }

    public ServiceOperationStatus save(List<MultipartFile> files, User user, Submission linkedSubmission) {
        if (usersService.hasAnyRole(user)) {
            List<FileAccessService.UserFileOperationResult> results = new ArrayList<>();
            for (MultipartFile file : files) {
                try {
                    Metadata meta = FileMetadataTool.getFrom(file, user.getId());
                    String filename = meta.getCreatedAt() + "." + meta.getFileExtension();

                    String storagePath = user.getId()
                        + FileSystems.getDefault().getSeparator()
                        + filename;
                    fileManager.saveFile(file.getInputStream(), storagePath);

                    UserFile fileDbObject = new UserFile(
                        null,
                        user,
                        filename,
                        storagePath,
                        meta.getMimeType(),
                        meta.getCreatedAt(),
                        linkedSubmission
                    );
                    fileRepository.save(fileDbObject);
                    results.add(
                        new FileAccessService.UserFileOperationResult(file, ServiceOperationStatus.SUCCESSFUL)
                    );
                    logger.info("Saved file: {}", fileDbObject);
                } catch (IOException e) {
                    results.add(
                        new FileAccessService.UserFileOperationResult(file, ServiceOperationStatus.CREATION_FAILED)
                    );
                }
            }
            boolean allSuccessful = results.stream()
                .allMatch(result -> result.status.equals(ServiceOperationStatus.SUCCESSFUL));
            return allSuccessful ? ServiceOperationStatus.SUCCESSFUL : ServiceOperationStatus.MULTI_STATUS_OPERATION;
        } else {
            return ServiceOperationStatus.NOT_ALLOWED_OPERATION;
        }
    }

    public ServiceOperationStatus delete(User user, Long file_id, Submission submission) {
        if (usersService.hasModeratorOrAdminRole(user)) {
            Optional<UserFile> optional = submission.getSubmitterDocuments().stream()
                .filter(file -> file.getId().equals(file_id))
                .findFirst();
            if (optional.isEmpty()) return ServiceOperationStatus.NOT_FOUND;
            UserFile file = optional.get();
            try {
                logger.warn("Deleting file: {}", file);
                fileManager.removeFile(file.getStoragePath());
                fileRepository.deleteById(file.getId());
                return ServiceOperationStatus.SUCCESSFUL;
            } catch (IOException e) {
                return ServiceOperationStatus.CREATION_FAILED;
            }
        } else {
            return ServiceOperationStatus.NOT_ALLOWED_OPERATION;
        }
    }

    private record DeletingUserFilesOperationResult(UserFile file, ServiceOperationStatus status) { }

    public ServiceOperationStatus deleteAll(User user, Submission submission) {
        if (usersService.hasModeratorOrAdminRole(user)) {
            List<FileAccessService.DeletingUserFilesOperationResult> results = new ArrayList<>();
            for (UserFile file : submission.getSubmitterDocuments()) {
                try {
                    logger.warn("Deleting file: {}", file);
                    fileManager.removeFile(file.getStoragePath());
                    fileRepository.deleteById(file.getId());
                    results.add(
                        new FileAccessService.DeletingUserFilesOperationResult(file, ServiceOperationStatus.SUCCESSFUL)
                    );
                } catch (IOException e) {
                    results.add(
                        new FileAccessService.DeletingUserFilesOperationResult(file, ServiceOperationStatus.CREATION_FAILED)
                    );
                }
            }
            boolean allSuccessful = results.stream()
                .allMatch(result -> result.status.equals(ServiceOperationStatus.SUCCESSFUL));
            return allSuccessful ? ServiceOperationStatus.SUCCESSFUL : ServiceOperationStatus.MULTI_STATUS_OPERATION;
        } else {
            return ServiceOperationStatus.NOT_ALLOWED_OPERATION;
        }
    }
}
