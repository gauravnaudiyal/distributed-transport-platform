package com.groupb.booking.service.client;

import com.groupb.booking.model.dto.RouteDecomposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@Profile("!mock")
public class HttpRouteServiceClient implements RouteServiceClient {

    private static final Logger log = LoggerFactory.getLogger(HttpRouteServiceClient.class);

    private final RestTemplate restTemplate;
    private final String routeServiceUrl;

    public HttpRouteServiceClient(
            RestTemplate restTemplate,
            @Value("${route.service.url:http://route-management-svc}") String routeServiceUrl) {
        this.restTemplate = restTemplate;
        this.routeServiceUrl = routeServiceUrl;
    }

    @Override
    public RouteDecomposition decompose(String origin, String destination, String mapVersion) {
        String url = routeServiceUrl + "/api/routes/decompose";

        Map<String, String> request = new HashMap<>();
        request.put("origin", origin);
        request.put("destination", destination);
        if (mapVersion != null) {
            request.put("map_version", mapVersion);
        }

        try {
            ResponseEntity<RouteDecomposition> response = restTemplate.postForEntity(url, request, RouteDecomposition.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Route decomposition failed with status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to call route-management service at {}", url, e);
            throw new RuntimeException("Failed to decompose route", e);
        }
    }
}
