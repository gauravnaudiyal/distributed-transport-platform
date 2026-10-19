import type { Booking, BookingRequest, RouteSegment } from '../store/booking';
import { useAuthStore } from '../store/auth';
import { httpClient } from './httpClient';

interface BackendBookingResponse {
    booking_id: string;
    user_id: string;
    state: Booking['state'];
    map_version: string;
    coordinator_region?: string;
    created_at: string;
}

interface BackendVerificationProof {
    booking_id: string;
    proof_token: string;
    map_version: string;
    time_window_start: string;
    time_window_end: string;
    segment_scope: string[];
}

function resolveUserId(): string {
    return useAuthStore.getState().userId || useAuthStore.getState().username || 'civilian-demo';
}

function makeIdempotencyKey(request: BookingRequest): string {
    const userId = resolveUserId();
    return [
        userId,
        request.origin,
        request.destination,
        request.departureTime,
        request.timeWindow,
    ].join(':');
}

function toBackendRequest(request: BookingRequest) {
    const departure = new Date(request.departureTime);
    const end = new Date(departure.getTime() + request.timeWindow * 60_000);

    return {
        user_id: resolveUserId(),
        origin: request.origin,
        destination: request.destination,
        time_window_start: departure.toISOString(),
        time_window_end: end.toISOString(),
        idempotency_key: makeIdempotencyKey(request),
    };
}

function toFrontendBooking(
    response: BackendBookingResponse,
    requestContext?: BookingRequest,
    verificationToken?: string
): Booking {
    const departureTime = requestContext?.departureTime || response.created_at;
    const arrivalTime = new Date(
        new Date(departureTime).getTime() + (requestContext?.timeWindow || 60) * 60_000
    ).toISOString();

    return {
        id: response.booking_id,
        origin: requestContext?.origin || 'Unavailable',
        destination: requestContext?.destination || 'Unavailable',
        departureTime,
        arrivalTime,
        segments: [],
        state: response.state,
        createdAt: response.created_at,
        verificationToken,
        mapVersion: response.map_version,
    };
}

async function fetchVerificationToken(bookingId: string): Promise<string | undefined> {
    const proof = await httpClient.get<BackendVerificationProof>(
        `/bookings/${encodeURIComponent(bookingId)}/verify`
    );
    return proof.proof_token;
}

export const bookingApiService = {
    async requestBooking(request: BookingRequest): Promise<Booking> {
        const booking = await httpClient.post<BackendBookingResponse>(
            '/bookings',
            toBackendRequest(request)
        );

        const verificationToken =
            booking.state === 'CONFIRMED'
                ? await fetchVerificationToken(booking.booking_id)
                : undefined;

        return toFrontendBooking(booking, request, verificationToken);
    },

    async getBooking(bookingId: string): Promise<Booking> {
        const booking = await httpClient.get<BackendBookingResponse>(
            `/bookings/${encodeURIComponent(bookingId)}`
        );
        const verificationToken =
            booking.state === 'CONFIRMED'
                ? await fetchVerificationToken(booking.booking_id)
                : undefined;

        return toFrontendBooking(booking, undefined, verificationToken);
    },

    async cancelBooking(bookingId: string): Promise<Booking> {
        const booking = await httpClient.post<BackendBookingResponse>(
            `/bookings/${encodeURIComponent(bookingId)}/cancel`
        );
        return toFrontendBooking(booking);
    },

    async getSegments(_origin: string, _destination: string): Promise<RouteSegment[]> {
        return [];
    },

    async getAllSegments(): Promise<RouteSegment[]> {
        return [];
    },

    async verifyBooking(bookingId: string, token: string): Promise<boolean> {
        const proof = await httpClient.get<BackendVerificationProof>(
            `/bookings/${encodeURIComponent(bookingId)}/verify`
        );
        return proof.proof_token === token;
    },
};
