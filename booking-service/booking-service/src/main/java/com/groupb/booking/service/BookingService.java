package com.groupb.booking.service;

import com.groupb.booking.model.dto.*;
import com.groupb.booking.model.entity.Booking;
import com.groupb.booking.model.entity.BookingSegment;
import com.groupb.booking.model.enums.BookingEventType;
import com.groupb.booking.model.enums.BookingState;
import com.groupb.booking.repository.BookingRepository;
import com.groupb.booking.service.client.HttpCompatibilityServiceClient;
import com.groupb.booking.service.client.RouteServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RouteServiceClient routeServiceClient;
    private final HttpCompatibilityServiceClient httpCompatibilityServiceClient;
    private final IdempotencyService idempotencyService;
    private final OutboxPublisher outboxPublisher;

    public BookingService(BookingRepository bookingRepository,
                          RouteServiceClient routeServiceClient,
                          HttpCompatibilityServiceClient httpCompatibilityServiceClient,
                          IdempotencyService idempotencyService,
                          OutboxPublisher outboxPublisher) {
        this.bookingRepository = bookingRepository;
        this.routeServiceClient = routeServiceClient;
        this.httpCompatibilityServiceClient = httpCompatibilityServiceClient;
        this.idempotencyService = idempotencyService;
        this.outboxPublisher = outboxPublisher;
    }

    /**
     * Creates a new booking, orchestrating the full saga:
     * PENDING -> HOLD segments -> RESERVED -> CONFIRM segments -> CONFIRMED
     */
    public BookingResponse createBooking(BookingRequest request) {
        // 1. Idempotency check
        if (!idempotencyService.tryAcquire(request.getIdempotencyKey())) {
            log.info("Duplicate request detected for idempotency key: {}", request.getIdempotencyKey());
            Optional<Booking> existing = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
            // Key was in Redis but booking not in DB yet — race condition, release and retry
            idempotencyService.release(request.getIdempotencyKey());
        }

        try {
            // 1.5. Check journey compatibility with external service
            JourneyCompatibilityResponse compatibilityResp = checkJourneyCompatibility(request);
            if (!"APPROVED".equals(compatibilityResp.getStatus())) {
                log.warn("Journey compatibility check rejected for booking request - Status: {}, Message: {}",
                         compatibilityResp.getStatus(), compatibilityResp.getMessage());
                idempotencyService.release(request.getIdempotencyKey());
                throw new RuntimeException("Journey compatibility check failed: " + compatibilityResp.getMessage());
            }

            // 2. Create booking in PENDING state
            Booking booking = createPendingBooking(request);

            // 3. Decompose route into segments
            RouteDecomposition route = routeServiceClient.decompose(
                    request.getOrigin(), request.getDestination(), null);
            booking.setMapVersion(route.getMapVersion());
            booking.setCoordinatorRegion(determineCoordinatorRegion(route));

            // 4. Persist segments
            List<BookingSegment> segments = persistSegments(booking, route);

            // 5. HOLD all segments (phase 1 of 2PC-lite)
            boolean allHeld = holdAllSegments(booking, segments);
            if (!allHeld) {
                rejectBooking(booking);
                return toResponse(booking);
            }

            // 6. Transition to RESERVED
            transitionState(booking, BookingState.RESERVED);

            // 7. CONFIRM all segments (phase 2)
            boolean allConfirmed = confirmAllSegments(booking, segments);
            if (!allConfirmed) {
                failBooking(booking, segments);
                return toResponse(booking);
            }

            // 8. Transition to CONFIRMED
            confirmBooking(booking);
            return toResponse(booking);

        } catch (Exception e) {
            log.error("Booking creation failed for idempotency key: {}", request.getIdempotencyKey(), e);
            idempotencyService.release(request.getIdempotencyKey());
            throw new RuntimeException("Booking creation failed", e);
        }
    }

    /**
     * Cancels a confirmed booking by releasing all segment reservations.
     * Evicts the booking from cache since its state changes.
     */
    @CacheEvict(value = "bookings", key = "#bookingId")
    public BookingResponse cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));

        if (booking.getState() != BookingState.CONFIRMED) {
            throw new IllegalStateException(
                    "Cannot cancel booking in state " + booking.getState() + ". Only CONFIRMED bookings can be cancelled.");
        }

        List<BookingSegment> segments = booking.getSegments();
        for (BookingSegment segment : segments) {
            releaseSegment(booking, segment);
            segment.setReservationStatus("RELEASED");
        }

        return cancelBookingState(booking);
    }

    /**
     * Retrieves a booking by ID.
     * Cached in Redis for 10 minutes to reduce database load.
     */
    @Cacheable(value = "bookings", key = "#bookingId")
    public BookingResponse getBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));
        return toResponse(booking);
    }

    /**
     * Generates a verification proof for enforcement.
     */
    public VerificationProof getVerificationProof(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + bookingId));

        if (booking.getState() != BookingState.CONFIRMED) {
            throw new IllegalStateException(
                    "Verification proof only available for CONFIRMED bookings. Current state: " + booking.getState());
        }

        List<String> segmentIds = booking.getSegments().stream()
                .map(BookingSegment::getSegmentId)
                .collect(Collectors.toList());

        String proofToken = Base64.getEncoder().encodeToString(
                (booking.getBookingId() + ":" + booking.getMapVersion() + ":" + booking.getCreatedAt())
                        .getBytes());

        return new VerificationProof(
                booking.getBookingId(),
                proofToken,
                booking.getMapVersion(),
                booking.getTimeWindowStart(),
                booking.getTimeWindowEnd(),
                segmentIds
        );
    }

    // ─── Private helpers ───────────────────────────────────────────────

    @Transactional
    protected Booking createPendingBooking(BookingRequest request) {
        Booking booking = new Booking();
        booking.setBookingId(UUID.randomUUID().toString());
        booking.setUserId(request.getUserId());
        booking.setOrigin(request.getOrigin());
        booking.setDestination(request.getDestination());
        booking.setState(BookingState.PENDING);
        booking.setIdempotencyKey(request.getIdempotencyKey());
        booking.setTimeWindowStart(request.getTimeWindowStart());
        booking.setTimeWindowEnd(request.getTimeWindowEnd());

        booking = bookingRepository.save(booking);
        outboxPublisher.publish(booking, BookingEventType.BOOKING_CREATED);
        log.info("Created PENDING booking: {}", booking.getBookingId());
        return booking;
    }

    @Transactional
    protected List<BookingSegment> persistSegments(Booking booking, RouteDecomposition route) {
        List<BookingSegment> segments = new ArrayList<>();
        for (RouteDecomposition.Segment seg : route.getSegments()) {
            BookingSegment bs = new BookingSegment();
            bs.setBooking(booking);
            bs.setSegmentId(seg.getSegmentId());
            bs.setAuthoritativeRegion(seg.getAuthoritativeRegion());
            bs.setReservationStatus("PENDING");
            bs.setTimeWindowStart(seg.getTimeWindowStart());
            bs.setTimeWindowEnd(seg.getTimeWindowEnd());
            segments.add(bs);
        }
        booking.getSegments().addAll(segments);
        bookingRepository.save(booking);
        return segments;
    }

    private boolean holdAllSegments(Booking booking, List<BookingSegment> segments) {
        List<BookingSegment> held = new ArrayList<>();
        for (BookingSegment segment : segments) {
            ReservationRequest req = buildReservationRequest(booking, segment, "HOLD");
            ReservationResponse resp = httpCompatibilityServiceClient.reserve(req);

            if ("RESERVED".equals(resp.getStatus())) {
                segment.setReservationStatus("RESERVED");
                held.add(segment);
            } else {
                log.warn("HOLD rejected for segment {} on booking {}: {}",
                        segment.getSegmentId(), booking.getBookingId(), resp.getReason());
                compensateHeldSegments(booking, held);
                return false;
            }
        }
        return true;
    }

    private boolean confirmAllSegments(Booking booking, List<BookingSegment> segments) {
        for (BookingSegment segment : segments) {
            ReservationRequest req = buildReservationRequest(booking, segment, "CONFIRM");
            ReservationResponse resp = httpCompatibilityServiceClient.reserve(req);

            if (!"CONFIRMED".equals(resp.getStatus())) {
                log.error("CONFIRM failed for segment {} on booking {}: {}",
                        segment.getSegmentId(), booking.getBookingId(), resp.getReason());
                return false;
            }
            segment.setReservationStatus("CONFIRMED");
        }
        return true;
    }

    private void compensateHeldSegments(Booking booking, List<BookingSegment> heldSegments) {
        for (BookingSegment segment : heldSegments) {
            releaseSegment(booking, segment);
            segment.setReservationStatus("RELEASED");
        }
    }

    private void releaseSegment(Booking booking, BookingSegment segment) {
        ReservationRequest req = buildReservationRequest(booking, segment, "RELEASE");
        try {
            httpCompatibilityServiceClient.reserve(req);
        } catch (Exception e) {
            log.error("Failed to release segment {} for booking {}",
                    segment.getSegmentId(), booking.getBookingId(), e);
        }
    }

    @Transactional
    protected void transitionState(Booking booking, BookingState newState) {
        booking.setState(newState);
        bookingRepository.save(booking);
        log.info("Booking {} transitioned to {}", booking.getBookingId(), newState);
    }

    @Transactional
    protected void rejectBooking(Booking booking) {
        booking.setState(BookingState.REJECTED);
        bookingRepository.save(booking);
        outboxPublisher.publish(booking, BookingEventType.BOOKING_REJECTED);
        log.info("Booking {} REJECTED", booking.getBookingId());
    }

    @Transactional
    protected void failBooking(Booking booking, List<BookingSegment> segments) {
        compensateHeldSegments(booking, segments);
        booking.setState(BookingState.FAILED);
        bookingRepository.save(booking);
        log.error("Booking {} FAILED during confirm phase", booking.getBookingId());
    }

    @Transactional
    @CacheEvict(value = "bookings", key = "#booking.bookingId")
    protected void confirmBooking(Booking booking) {
        booking.setState(BookingState.CONFIRMED);
        bookingRepository.save(booking);
        outboxPublisher.publish(booking, BookingEventType.BOOKING_CONFIRMED);
        log.info("Booking {} CONFIRMED", booking.getBookingId());
    }

    @Transactional
    protected BookingResponse cancelBookingState(Booking booking) {
        booking.setState(BookingState.CANCELLED);
        bookingRepository.save(booking);
        outboxPublisher.publish(booking, BookingEventType.BOOKING_CANCELLED);
        log.info("Booking {} CANCELLED", booking.getBookingId());
        return toResponse(booking);
    }

    private ReservationRequest buildReservationRequest(Booking booking, BookingSegment segment, String action) {
        return new ReservationRequest(
                booking.getBookingId(),
                segment.getSegmentId(),
                segment.getAuthoritativeRegion(),
                segment.getTimeWindowStart(),
                segment.getTimeWindowEnd(),
                action
        );
    }

    private String determineCoordinatorRegion(RouteDecomposition route) {
        if (route.getSegments() != null && !route.getSegments().isEmpty()) {
            return route.getSegments().get(0).getAuthoritativeRegion();
        }
        return "default";
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getBookingId(),
                booking.getUserId(),
                booking.getState(),
                booking.getMapVersion(),
                booking.getCoordinatorRegion(),
                booking.getCreatedAt()
        );
    }

    /**
     * Checks journey compatibility with the external journey-compatibility-service.
     * Maps the incoming BookingRequest to the expected compatibility service request format.
     */
    private JourneyCompatibilityResponse checkJourneyCompatibility(BookingRequest request) {
        return httpCompatibilityServiceClient.checkJourneyCompatibility(
                request.getIdempotencyKey(),          // bookingId
                request.getOrigin(),                   // origin
                request.getDestination(),              // destination
                request.getTimeWindowStart(),          // travelDatetime
                request.getUserId(),                   // driverId
                4,                                     // passengerCount (default to 4)
                "CAR"                                  // vehicleType (default to CAR)
        );
    }
}
