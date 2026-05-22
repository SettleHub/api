package org.settlehub.discovery.core;

import org.settlehub.commons.health.annotation.EnableHealthCheck;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
@EnableHealthCheck
public class DiscoveryService {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryService.class, args);
	}

}
