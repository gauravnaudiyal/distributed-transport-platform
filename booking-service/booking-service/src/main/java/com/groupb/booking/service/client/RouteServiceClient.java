package com.groupb.booking.service.client;

import com.groupb.booking.model.dto.RouteDecomposition;

public interface RouteServiceClient {

    RouteDecomposition decompose(String origin, String destination, String mapVersion);
}
