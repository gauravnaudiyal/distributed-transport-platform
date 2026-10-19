package com.gtbs.adminauthority.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    public static final String ROUTE_MANAGEMENT_REST_CLIENT = "routeManagementRestClient";
    public static final String BOOKING_REST_CLIENT = "bookingRestClient";

    @Bean
    @Qualifier(ROUTE_MANAGEMENT_REST_CLIENT)
    public RestClient routeManagementRestClient(GtbsProperties gtbs) {
        return build(gtbs.routeManagement().baseUrl(), gtbs.routeManagement().timeout());
    }

    @Bean
    @Qualifier(BOOKING_REST_CLIENT)
    public RestClient bookingRestClient(GtbsProperties gtbs) {
        return build(gtbs.booking().baseUrl(), gtbs.booking().timeout());
    }

    private static RestClient build(String baseUrl, java.time.Duration timeout) {
        long timeoutMs = timeout != null ? timeout.toMillis() : 5_000;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) timeoutMs);
        factory.setReadTimeout((int) timeoutMs);
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
}
