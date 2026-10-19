package com.groupb.booking.service.client.mock;

import com.groupb.booking.model.dto.RouteDecomposition;
import com.groupb.booking.service.client.RouteServiceClient;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@Profile("mock")
public class MockRouteServiceClient implements RouteServiceClient {

    @Override
    public RouteDecomposition decompose(String origin, String destination, String mapVersion) {
        String version = (mapVersion != null) ? mapVersion : "v2025.03";

        RouteDecomposition.Segment seg1 = new RouteDecomposition.Segment(
                "seg-" + UUID.randomUUID().toString().substring(0, 8),
                "eu-west-1",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        RouteDecomposition.Segment seg2 = new RouteDecomposition.Segment(
                "seg-" + UUID.randomUUID().toString().substring(0, 8),
                "eu-west-2",
                Instant.now().plusSeconds(3600),
                Instant.now().plusSeconds(7200)
        );

        return new RouteDecomposition(version, List.of(seg1, seg2));
    }
}
