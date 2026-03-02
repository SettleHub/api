package org.ossfmct.projects.hostels.chessboard.config;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@JsonSerialize
public abstract class HostelViewConfiguration {

    private String hostelNumber;

}
