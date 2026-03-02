package org.ossfmct.projects.spreadsheets.config;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("sheets")
@JsonSerialize
public class SettlementSheetConfig {

    @Value("${sheets.sheet-cells.settlement.rooms.bed-place}")
    private String bedPlace;

    @Value("${sheets.sheet-cells.settlement.rooms.room-number}")
    private String roomNumber;

    @Value("${sheets.sheet-cells.settlement.rooms.full-name}")
    private String fullName;

    @Value("${sheets.sheet-cells.settlement.rooms.gender}")
    private String gender;

    @Value("${sheets.sheet-cells.settlement.rooms.university-faculty-institute}")
    private String universityFacultyInstitute;

    @Value("${sheets.sheet-cells.settlement.rooms.course-and-group}")
    private String courseAndGroup;

    @Value("${sheets.sheet-cells.settlement.rooms.contract-number}")
    private String contractNumber;

    @Value("${sheets.sheet-cells.settlement.rooms.contact-phone}")
    private String contactPhone;

    @Value("${sheets.sheet-cells.settlement.rooms.applicationplan2023-24}")
    private String applicationplan202324;

    @Value("${sheets.sheet-cells.settlement.rooms.settled}")
    private String settled;

    @Value("${sheets.sheet-cells.settlement.rooms.places}")
    private String places;

    @Value("${sheets.sheet-cells.settlement.total-settled}")
    private String totalSettled;

    @Value("${sheets.sheet-cells.settlement.total-places}")
    private String totalPlaces;

    @Value("${sheets.indent-between-rooms}")
    private String indentBetweenRooms;

}