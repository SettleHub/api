package org.ossfmct.projects.hostels.services;

import org.ossfmct.projects.hostels.enums.HostelVisibility;
import org.ossfmct.projects.hostels.models.Hostel;
import org.ossfmct.projects.hostels.repositories.HostelRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class HostelService {
    private final HostelRepository hostelRepository;

    public HostelService(HostelRepository hostelRepository) {
        this.hostelRepository = hostelRepository;
    };

    /***
     *
     * @param number
     * @param title
     * @param description
     * @param address
     * @param phone
     * @param visibility
     * @return Hostel created object
     */
    public Hostel createHostel(Integer number, String title, String description, String address, String phone, HostelVisibility visibility) {
        Hostel newHostel = new Hostel(number, title, description, address, phone, visibility) ;
        return hostelRepository.save(newHostel);
    }

    public List<Hostel> getAllHostels() {
        return hostelRepository.findAll().stream()
            .filter(hostel -> (
                hostel.getVisibility().equals(HostelVisibility.VISIBLE)))
            .toList();
    }

    public Optional<Hostel> getHostelColumn(long id) {
        return hostelRepository.findById(id);
    }

    public Optional<Hostel> getHostelByNumber(int number) {
        return hostelRepository.findByNumber(number);
    }

    public Hostel saveHostel(Hostel hostel){
        return hostelRepository.save(hostel);
    }
}