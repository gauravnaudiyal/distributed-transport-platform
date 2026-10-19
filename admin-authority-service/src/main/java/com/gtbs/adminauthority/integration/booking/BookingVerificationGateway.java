package com.gtbs.adminauthority.integration.booking;

import com.gtbs.adminauthority.api.dto.BookingVerificationResponse;
import com.gtbs.adminauthority.config.RestClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class BookingVerificationGateway {

    private static final String VERIFY_PATH = "/api/v1/internal/bookings/{bookingId}/authority-verification";

    private final RestClient bookingRestClient;

    public BookingVerificationGateway(@Qualifier(RestClientConfig.BOOKING_REST_CLIENT) RestClient bookingRestClient) {
        this.bookingRestClient = bookingRestClient;
    }

    public BookingVerificationResponse verify(String bookingId) {
        try {
            return bookingRestClient
                    .get()
                    .uri(VERIFY_PATH, bookingId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(BookingVerificationResponse.class);
        } catch (RestClientResponseException ex) {
            throw new BookingServiceException(
                    "Booking verification failed: HTTP %s".formatted(ex.getStatusCode()), ex);
        } catch (RestClientException ex) {
            throw new BookingServiceException("Booking Service unavailable", ex);
        }
    }
}
