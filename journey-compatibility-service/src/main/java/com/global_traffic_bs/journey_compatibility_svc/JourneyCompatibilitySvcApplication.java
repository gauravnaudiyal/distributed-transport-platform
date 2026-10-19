package com.global_traffic_bs.journey_compatibility_svc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class JourneyCompatibilitySvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(JourneyCompatibilitySvcApplication.class, args);
	}

}