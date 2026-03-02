package org.ossfmct.projects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author <a href="https://github.com/sc-fmct-tech">sc-fmct-tech</a>
 * 
 */

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan("org.ossfmct.projects.security")
@ComponentScan("org.ossfmct.projects.tools")
@ComponentScan("org.ossfmct.projects.spreadsheets")
@ComponentScan("org.ossfmct.projects.hostels")
@ComponentScan("org.ossfmct.projects.users")
@ComponentScan("org.ossfmct.projects.submissions")
@ComponentScan("org.ossfmct.projects.files")
public class EsettlementApi {
    public static void main(String[] args) {
        SpringApplication.run(EsettlementApi.class, args);
    }
}
