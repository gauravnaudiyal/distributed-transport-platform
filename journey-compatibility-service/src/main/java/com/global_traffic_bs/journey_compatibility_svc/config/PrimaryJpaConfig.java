package com.global_traffic_bs.journey_compatibility_svc.config;

import com.global_traffic_bs.journey_compatibility_svc.repository.BookingRecordRepository;
import com.global_traffic_bs.journey_compatibility_svc.repository.RouteCapacityRepository;
import com.global_traffic_bs.journey_compatibility_svc.repository.booking.BookingRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = {RouteCapacityRepository.class, BookingRecordRepository.class},
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = BookingRepository.class
        ),
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class PrimaryJpaConfig {
}


