package com.gtbs.adminauthority.integration.rms;

import com.gtbs.adminauthority.api.dto.CapacityOverrideRequest;
import com.gtbs.adminauthority.api.dto.ClosureRequest;
import com.gtbs.adminauthority.api.dto.RestrictionRequest;
import com.gtbs.adminauthority.api.dto.SegmentForecastResponse;
import com.gtbs.adminauthority.config.RestClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RouteManagementGateway {

    static final String CLOSURES_PATH = "/api/v1/internal/authority/closures";
    static final String RESTRICTIONS_PATH = "/api/v1/internal/authority/restrictions";
    static final String CAPACITY_PATH = "/api/v1/internal/authority/capacity-overrides";
    static final String FORECAST_PATH = "/api/v1/internal/segments/{segmentId}/forecast";

    private final RestClient routeManagementRestClient;

    public RouteManagementGateway(@Qualifier(RestClientConfig.ROUTE_MANAGEMENT_REST_CLIENT) RestClient routeManagementRestClient) {
        this.routeManagementRestClient = routeManagementRestClient;
    }

    public RmsApplyResponse applyClosure(ClosureRequest request) {
        return post(CLOSURES_PATH, request);
    }

    public RmsApplyResponse applyRestriction(RestrictionRequest request) {
        return post(RESTRICTIONS_PATH, request);
    }

    public RmsApplyResponse applyCapacityOverride(CapacityOverrideRequest request) {
        return post(CAPACITY_PATH, request);
    }

    public SegmentForecastResponse getForecast(String segmentId) {
        try {
            return routeManagementRestClient
                    .get()
                    .uri(FORECAST_PATH, segmentId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(SegmentForecastResponse.class);
        } catch (RestClientResponseException ex) {
            throw new RouteManagementException(
                    "Route Management forecast failed: HTTP %s".formatted(ex.getStatusCode()), ex);
        } catch (RestClientException ex) {
            throw new RouteManagementException("Route Management unavailable: forecast", ex);
        }
    }

    private RmsApplyResponse post(String uri, Object body) {
        try {
            RmsApplyResponse res = routeManagementRestClient
                    .post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(RmsApplyResponse.class);
            if (res == null || res.mapVersion() == null) {
                throw new RouteManagementException("Route Management returned empty apply response");
            }
            return res;
        } catch (RestClientResponseException ex) {
            throw new RouteManagementException(
                    "Route Management rejected instruction: HTTP %s — %s"
                            .formatted(ex.getStatusCode(), ex.getResponseBodyAsString()),
                    ex);
        } catch (RestClientException ex) {
            throw new RouteManagementException("Route Management unavailable", ex);
        }
    }
}
