package org.ossfmct.projects.hostels.chessboard.config;

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
@ConfigurationProperties("view")
@JsonSerialize
public class HostelThreeViewConfiguration extends HostelViewConfiguration {

    public HostelThreeViewConfiguration() {
        this.setHostelNumber("3");
    }

    @Value("${view.hostel.three.firstRoom}")
    private Short firstRoom;

    @Value("${view.hostel.three.lastRoom}")
    private Short lastRoom;

    @Value("${view.hostel.three.roomsCountOnFloor}")
    private Short roomsCountOnFloor;

    @Value("${view.hostel.three.additionalRooms}")
    private String additionalRooms;

    @Value("${view.hostel.three.additionalRoomSymbol}")
    private String additionalRoomSymbol;

}
