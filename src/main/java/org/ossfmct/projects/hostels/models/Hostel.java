package org.ossfmct.projects.hostels.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ossfmct.projects.hostels.enums.HostelVisibility;

/**
 * NoArgsConstructor needs for JPA.
 */
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "hostels")
public class Hostel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("number")
    @NotNull
    @Column(name = "number")
    private Integer number;

    @JsonProperty("title")
    @NotNull
    @Column(name = "title")
    private String title;

    @JsonProperty("description")
    @NotNull
    @Column(name = "description")
    private String description;

    @JsonProperty("address")
    @NotNull
    @Column(name = "address")
    private String address;

    @JsonProperty("phone")
    @NotNull
    @Pattern(regexp = "^\\+\\d{1,3}\\s?\\d{1,14}(\\s\\d{1,13})?$", message = "Invalid phone number format")
    @Column(name = "phone")
    private String phone;

    @NotNull
    @Column(name = "visibility")
    private HostelVisibility visibility;

    public Hostel(Integer number, String title, String description, String address, String phone, HostelVisibility  visibility) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.visibility = visibility;
    }

    public Hostel(Integer number, String title, String description, Address address, String phone, HostelVisibility  visibility) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.address = address.simpleStringFormat();
        this.phone = phone;
        this.visibility = visibility;
    }
}