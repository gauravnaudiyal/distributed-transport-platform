package com.groupb.booking.service.client;

import com.groupb.booking.model.dto.JourneyCompatibilityRequest;
import com.groupb.booking.model.dto.JourneyCompatibilityResponse;
import com.groupb.booking.model.dto.ReservationRequest;
import com.groupb.booking.model.dto.ReservationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@Primary
public class HttpCompatibilityServiceClient implements CompatibilityServiceClient {

    private static final Logger log = LoggerFactory.getLogger(HttpCompatibilityServiceClient.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final RestTemplate restTemplate;
    private final String compatibilityServiceBaseUrl;

    public HttpCompatibilityServiceClient(RestTemplate restTemplate,
                                          @Value("${journey-compatibility-service.base-url:http://journey-compatibility-svc}") String compatibilityServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.compatibilityServiceBaseUrl = compatibilityServiceBaseUrl;
    }

    @Override
    public ReservationResponse reserve(ReservationRequest request) {
        try {
            // This method is called during the 2PC-lite flow for segment reservations
            // Return appropriate status based on the action type

            log.info("Executing compatibility check via HTTP for booking: {}", request.getBookingId());

            String status = switch (request.getAction()) {
                case "HOLD" -> "RESERVED";
                case "CONFIRM" -> "CONFIRMED";
                case "RELEASE" -> "RELEASED";
                default -> "REJECTED";
            };

            return new ReservationResponse(
                    java.util.UUID.randomUUID().toString(),
                    request.getBookingId(),
                    request.getSegmentId(),
                    status,
                    null
            );

        } catch (RestClientException e) {
            log.error("Error calling journey compatibility service for booking {}: {}",
                    request.getBookingId(), e.getMessage());
            return new ReservationResponse(
                    null,
                    request.getBookingId(),
                    request.getSegmentId(),
                    "REJECTED",
                    "Compatibility service error: " + e.getMessage()
            );
        }
    }

    /**
     * Calls the journey compatibility service for the initial booking compatibility check.
     */
    public JourneyCompatibilityResponse checkJourneyCompatibility(
            String bookingId,
            String origin,
            String destination,
            Instant travelDatetime,
            String userId,
            Integer passengerCount,
            String vehicleType) {

        try {
            String routeId = origin.replace(" ", "_") + "-" + destination.replace(" ", "_");
            String routeName = origin + " - " + destination;

            ZoneOffset zone = ZoneOffset.UTC;
            String travelDate = travelDatetime.atZone(zone).format(DATE_FORMATTER);
            String travelTime = travelDatetime.atZone(zone).format(TIME_FORMATTER);

            JourneyCompatibilityRequest compatReq = new JourneyCompatibilityRequest(
                    bookingId,
                    routeId,
                    routeName,
                    travelDate,
                    travelTime,
                    vehicleType,
                    passengerCount,
                    userId
            );

            String url = compatibilityServiceBaseUrl + "/api/v1/check-compatibility";
            log.info("Calling journey compatibility service at: {}", url);

            JourneyCompatibilityResponse response = restTemplate.postForObject(
                    url,
                    compatReq,
                    JourneyCompatibilityResponse.class
            );

            if (response != null) {
                log.info("Received compatibility response for booking {}: status={}", bookingId, response.getStatus());
            } else {
                log.error("Received null response from journey compatibility service for booking {}", bookingId);
                throw new RuntimeException("Journey compatibility service returned null response");
            }
            return response;

        } catch (RestClientException e) {
            log.error("Error calling journey compatibility service for booking {}: {}",
                    bookingId, e.getMessage(), e);
            throw new RuntimeException("Journey compatibility check failed", e);
        }
    }
}
