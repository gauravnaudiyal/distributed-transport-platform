package com.groupb.booking.service.client;

import com.groupb.booking.model.dto.ReservationRequest;
import com.groupb.booking.model.dto.ReservationResponse;

public interface CompatibilityServiceClient {

    ReservationResponse reserve(ReservationRequest request);
}
