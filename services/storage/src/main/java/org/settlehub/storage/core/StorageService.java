package org.settlehub.storage.core;

import org.settlehub.commons.health.annotation.EnableHealthCheck;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableHealthCheck
@ComponentScan(basePackages = {"org.settlehub.storage"})
public class StorageService {

	public static void main(String[] args) {
		SpringApplication.run(StorageService.class, args);
	}

}
