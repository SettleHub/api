package org.ossfmct.projects.hostels.controllers;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.ossfmct.projects.hostels.enums.HostelVisibility;
import org.ossfmct.projects.hostels.models.Hostel;
import org.ossfmct.projects.hostels.services.HostelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/hostels")
public class HostelController {

    private final HostelService hostelService;

    @GetMapping("/")
    public ResponseEntity<?> getHostels(
        @RequestParam(value = "number", required = false, defaultValue = "0") Integer number) {
        if (number.equals(0)) {
            List<Hostel> hostels = hostelService.getAllHostels();
            if (!hostels.isEmpty()) {
                return ResponseEntity.ok(hostels);
            }
        } else {
            Optional<Hostel> hostel = hostelService.getHostelByNumber(number);
            if (hostel.isPresent() && !hostel.get().getVisibility().equals(HostelVisibility.UNVISIBLE)) {
                return ResponseEntity.ok(hostel.get());
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hostels for your request wasn't found.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHostelById(@PathVariable("id") int id) {
        Optional<Hostel> hostel = hostelService.getHostelColumn(id);
        if (hostel.isEmpty() || hostel.get().getVisibility().equals(HostelVisibility.UNVISIBLE)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hostel wasn't found.");
        }
        return ResponseEntity.ok(hostel.get());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MODERATOR', 'ROLE_ADMIN')")
    @PostMapping("/save")
    public ResponseEntity<Hostel> createHostel(@RequestBody Hostel hostel) {
        Hostel savedHostel = hostelService.saveHostel(hostel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHostel);
    }
}
