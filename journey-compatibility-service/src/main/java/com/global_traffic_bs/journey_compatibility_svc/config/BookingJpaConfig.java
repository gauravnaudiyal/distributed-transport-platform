package com.global_traffic_bs.journey_compatibility_svc.config;

import com.global_traffic_bs.journey_compatibility_svc.repository.booking.BookingRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = BookingRepository.class,
        entityManagerFactoryRef = "bookingEntityManagerFactory",
        transactionManagerRef = "bookingTransactionManager"
)
public class BookingJpaConfig {
}


