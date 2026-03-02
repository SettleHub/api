package org.ossfmct.projects.files;

import org.ossfmct.projects.security.models.UserDetailsImpl;
import org.ossfmct.projects.users.models.User;
import org.ossfmct.projects.users.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;

@RestController
@RequestMapping("/documents/")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private UsersService usersService;

    @Autowired
    private FileAccessService service;

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("load")
    public ResponseEntity<?> findAllByOwner(@RequestParam("id") Long id, Authentication authentication) {
        Optional<User> optionalUser = usersService.getUserFromAuthentication(authentication);
        if (optionalUser.isEmpty()) {
            logger.warn("Access denied for {}", ((UserDetailsImpl) authentication.getPrincipal()).toString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Compromised user.");
        }
        Optional<Resource> resource = service.load(id, optionalUser.get());
        if (resource.isPresent()) {
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.get().getFilename() + "\"")
                .body(resource.get());
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot download resource.");
        }
    }
}
