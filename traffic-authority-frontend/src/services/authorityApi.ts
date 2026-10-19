import { httpClient } from './httpClient';
import type {
    AppliedInstructionResponse,
    AuthorityAuditEntry,
    AuthorityBookingVerificationResponse,
    AuthorityCapacityOverrideRequest,
    AuthorityClosureRequest,
    AuthorityRestrictionRequest,
    SegmentForecastResponse,
} from '../types/authority';

const API_BASE =
    import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_BASE || '/api';

const AUTHORITY_BASE =
    import.meta.env.VITE_AUTHORITY_API_BASE_URL ||
    API_BASE ||
    import.meta.env.VITE_GATEWAY_BASE_URL ||
    '/api';

export const authorityApiService = {
    createClosure(payload: AuthorityClosureRequest) {
        return httpClient.post<AppliedInstructionResponse>('/authority/closures', payload, {
            baseUrl: AUTHORITY_BASE,
        });
    },

    createRestriction(payload: AuthorityRestrictionRequest) {
        return httpClient.post<AppliedInstructionResponse>(
            '/authority/restrictions',
            payload,
            {
                baseUrl: AUTHORITY_BASE,
            }
        );
    },

    createCapacityOverride(payload: AuthorityCapacityOverrideRequest) {
        return httpClient.post<AppliedInstructionResponse>(
            '/authority/capacity-overrides',
            payload,
            {
                baseUrl: AUTHORITY_BASE,
            }
        );
    },

    verifyBooking(bookingId: string) {
        return httpClient.get<AuthorityBookingVerificationResponse>(
            `/authority/bookings/${encodeURIComponent(bookingId)}/verify`,
            {
                baseUrl: AUTHORITY_BASE,
            }
        );
    },

    getSegmentForecast(segmentId: string) {
        return httpClient.get<SegmentForecastResponse>(
            `/authority/segments/${encodeURIComponent(segmentId)}/forecast`,
            {
                baseUrl: AUTHORITY_BASE,
            }
        );
    },

    getSegmentAudit(segmentId: string) {
        return httpClient.get<AuthorityAuditEntry[]>(
            `/authority/audit/${encodeURIComponent(segmentId)}`,
            {
                baseUrl: AUTHORITY_BASE,
            }
        );
    },
};
