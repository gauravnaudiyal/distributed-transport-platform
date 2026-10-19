import { httpClient } from './httpClient';
import type { AuditRecord } from '../types/audit';

const API_BASE =
    import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_BASE || '/api';

const AUDIT_BASE =
    import.meta.env.VITE_AUDIT_API_BASE_URL ||
    API_BASE ||
    import.meta.env.VITE_GATEWAY_BASE_URL ||
    '/api';

export const auditApiService = {
    getBookingAudit(bookingId: string): Promise<AuditRecord[]> {
        return httpClient.get<AuditRecord[]>(
            `/audit/bookings/${encodeURIComponent(bookingId)}`,
            {
                baseUrl: AUDIT_BASE,
            }
        );
    },

    getSegmentAudit(segmentId: string): Promise<AuditRecord[]> {
        return httpClient.get<AuditRecord[]>(
            `/audit/segments/${encodeURIComponent(segmentId)}`,
            {
                baseUrl: AUDIT_BASE,
            }
        );
    },
};
