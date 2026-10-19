package com.gtbs.adminauthority;

import com.gtbs.adminauthority.config.AppSecurityProperties;
import com.gtbs.adminauthority.config.GtbsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({GtbsProperties.class, AppSecurityProperties.class})
public class AdminAuthorityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminAuthorityServiceApplication.class, args);
    }
}
