package com.groupb.booking;

import com.groupb.booking.model.dto.BookingRequest;
import com.groupb.booking.model.dto.BookingResponse;
import com.groupb.booking.model.dto.VerificationProof;
import com.groupb.booking.model.enums.BookingState;
import com.groupb.booking.repository.BookingRepository;
import com.groupb.booking.repository.OutboxEventRepository;
import com.groupb.booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOperations;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        outboxEventRepository.deleteAll();

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);
    }

    private BookingRequest makeRequest(String userId, String origin, String destination) {
        return new BookingRequest(
                userId, origin, destination,
                Instant.now(), Instant.now().plusSeconds(7200),
                UUID.randomUUID().toString()
        );
    }

    @Test
    void createBooking_happyPath_shouldReturnConfirmed() {
        BookingRequest request = makeRequest("user-1", "Dublin", "Cork");

        BookingResponse response = bookingService.createBooking(request);

        assertNotNull(response.getBookingId());
        assertEquals("user-1", response.getUserId());
        assertEquals(BookingState.CONFIRMED, response.getState());
        assertNotNull(response.getMapVersion());
        assertNotNull(response.getCoordinatorRegion());
        assertNotNull(response.getCreatedAt());

        long eventCount = outboxEventRepository.count();
        assertEquals(2, eventCount); // CREATED + CONFIRMED
    }

    @Test
    void createBooking_duplicateIdempotencyKey_shouldReturnSameBooking() {
        String idempotencyKey = UUID.randomUUID().toString();

        BookingRequest request = new BookingRequest(
                "user-2", "Dublin", "Galway",
                Instant.now(), Instant.now().plusSeconds(3600),
                idempotencyKey
        );

        BookingResponse first = bookingService.createBooking(request);

        // Simulate Redis returning false (key already exists)
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);

        BookingResponse second = bookingService.createBooking(request);

        assertEquals(first.getBookingId(), second.getBookingId());
        assertEquals(first.getState(), second.getState());
    }

    @Test
    void cancelBooking_confirmedBooking_shouldReturnCancelled() {
        BookingRequest request = makeRequest("user-3", "Limerick", "Waterford");

        BookingResponse created = bookingService.createBooking(request);
        assertEquals(BookingState.CONFIRMED, created.getState());

        BookingResponse cancelled = bookingService.cancelBooking(created.getBookingId());
        assertEquals(BookingState.CANCELLED, cancelled.getState());
    }

    @Test
    void getVerificationProof_confirmedBooking_shouldReturnProof() {
        BookingRequest request = makeRequest("user-4", "Belfast", "Dublin");

        BookingResponse created = bookingService.createBooking(request);
        assertEquals(BookingState.CONFIRMED, created.getState());

        VerificationProof proof = bookingService.getVerificationProof(created.getBookingId());

        assertNotNull(proof.getProofToken());
        assertEquals(created.getBookingId(), proof.getBookingId());
        assertNotNull(proof.getSegmentScope());
        assertFalse(proof.getSegmentScope().isEmpty());
    }

    @Test
    void cancelBooking_nonConfirmedBooking_shouldThrow() {
        BookingRequest request = makeRequest("user-5", "Dublin", "Cork");

        BookingResponse created = bookingService.createBooking(request);
        bookingService.cancelBooking(created.getBookingId());

        assertThrows(IllegalStateException.class, () ->
                bookingService.cancelBooking(created.getBookingId()));
    }
}
