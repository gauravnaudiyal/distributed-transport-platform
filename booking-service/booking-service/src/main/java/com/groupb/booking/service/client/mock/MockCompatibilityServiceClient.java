package com.groupb.booking.service.client.mock;

import com.groupb.booking.model.dto.ReservationRequest;
import com.groupb.booking.model.dto.ReservationResponse;
import com.groupb.booking.service.client.CompatibilityServiceClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockCompatibilityServiceClient implements CompatibilityServiceClient {

    @Override
    public ReservationResponse reserve(ReservationRequest request) {
        String status = switch (request.getAction()) {
            case "HOLD" -> "RESERVED";
            case "CONFIRM" -> "CONFIRMED";
            case "RELEASE" -> "RELEASED";
            default -> "REJECTED";
        };

        return new ReservationResponse(
                UUID.randomUUID().toString(),
                request.getBookingId(),
                request.getSegmentId(),
                status,
                null
        );
    }
}
