package com.global_traffic_bs.journey_compatibility_svc.util;

import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;

/**
 * Utility class for normalizing route identifiers and names.
 * Ensures consistent route representation by alphabetically sorting route endpoints.
 */
@Slf4j
public class RouteNormalizer {

    private static final String ROUTE_SEPARATOR = "-";
    private static final String ROUTE_NAME_SEPARATOR = " - ";

    /**
     * Normalizes a route ID by alphabetically sorting the endpoints.
     * Example: "GALWAY-DUBLIN" -> "DUBLIN-GALWAY"
     *
     * @param routeId the original route ID
     * @return the normalized route ID
     */
    public static String normalizeRouteId(String routeId) {
        if (routeId == null || !routeId.contains(ROUTE_SEPARATOR)) {
            return routeId;
        }
        return normalizeRoute(routeId, ROUTE_SEPARATOR);
    }

    /**
     * Normalizes a route name by alphabetically sorting the endpoints.
     * Example: "GALWAY - DUBLIN" -> "DUBLIN - GALWAY"
     *
     * @param routeName the original route name
     * @return the normalized route name
     */
    public static String normalizeRouteName(String routeName) {
        if (routeName == null || !routeName.contains(ROUTE_NAME_SEPARATOR)) {
            return routeName;
        }
        return normalizeRoute(routeName, ROUTE_NAME_SEPARATOR);
    }

    /**
     * Internal method to normalize routes by splitting on separator, sorting, and rejoining.
     *
     * @param route the route string
     * @param separator the separator to use
     * @return the normalized route
     */
    private static String normalizeRoute(String route, String separator) {
        String[] endpoints = route.split(separator);

        if (endpoints.length != 2) {
            log.warn("Route '{}' does not have exactly 2 endpoints separated by '{}'. Returning as-is.", route, separator);
            return route;
        }

        String endpoint1 = endpoints[0].trim();
        String endpoint2 = endpoints[1].trim();

        // Sort alphabetically
        String[] sorted = {endpoint1, endpoint2};
        Arrays.sort(sorted);

        String normalized = String.join(separator, sorted);

        if (!route.equals(normalized)) {
            log.debug("Normalized route from '{}' to '{}'", route, normalized);
        }

        return normalized;
    }
}

