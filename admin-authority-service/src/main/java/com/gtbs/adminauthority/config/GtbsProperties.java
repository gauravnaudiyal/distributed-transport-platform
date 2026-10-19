package com.gtbs.adminauthority.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gtbs")
public record GtbsProperties(
        String regionId,
        RouteManagement routeManagement,
        Booking booking,
        Kafka kafka) {

    public record RouteManagement(String baseUrl, Duration timeout) {}

    public record Booking(String baseUrl, Duration timeout) {}

    public record Kafka(String authorityTopic) {}
}
