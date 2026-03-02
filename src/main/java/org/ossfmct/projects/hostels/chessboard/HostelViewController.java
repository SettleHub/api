package org.ossfmct.projects.hostels.chessboard;

import org.ossfmct.projects.hostels.chessboard.interfaces.IHostelViewController;
import org.ossfmct.projects.hostels.chessboard.interfaces.IHostelViewService;
import org.ossfmct.projects.hostels.chessboard.models.ViewHostel;
import org.ossfmct.projects.security.models.UserDetailsImpl;
import org.ossfmct.projects.submissions.enums.ServiceOperationStatus;
import org.ossfmct.projects.submissions.enums.SubmissionStatus;
import org.ossfmct.projects.users.models.User;
import org.ossfmct.projects.users.services.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/view/hostel")
public class HostelViewController implements IHostelViewController {
    private final IHostelViewService hostelViewService;
    private final Logger logger = LoggerFactory.getLogger(HostelViewController.class);

    @Autowired
    private UsersService usersService;

    public HostelViewController(IHostelViewService hostelViewService) {
        this.hostelViewService = hostelViewService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_MODERATOR', 'ROLE_ADMIN')")
    @GetMapping("/{number}")
    public ResponseEntity<?> getDataForHostelView(Authentication authentication,
                                           @PathVariable("number") String hostelNumber) throws IOException {
        Optional<User> optionalUser = usersService.getUserFromAuthentication(authentication);
        if (optionalUser.isEmpty()) {
            logger.warn("Access denied for {}", ((UserDetailsImpl) authentication.getPrincipal()).toString());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Compromised user.");
        }
        User user = optionalUser.get();

        ViewHostel viewHostel = hostelViewService.getViewHostel(hostelNumber);
        if (usersService.hasAnyRole(user)) {
            if (viewHostel == null) {
                logger.error("Hostel view not found for number: {}", hostelNumber);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hostel view not found for number: " + hostelNumber);
            }
        } else {
            logger.warn("Access denied for {}", user);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }
        return ResponseEntity.ok(viewHostel);
    }
}