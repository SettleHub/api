package org.settlehub.payment.core;

import org.settlehub.commons.health.annotation.EnableHealthCheck;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableHealthCheck
@EnableKafka
@ComponentScan(basePackages = {"org.settlehub.payment"})
public class PaymentService {

	public static void main(String[] args) {
		SpringApplication.run(PaymentService.class, args);
	}

}
