export interface AuditRecord {
    id: string;
    eventType: string;
    bookingId?: string;
    segmentId?: string;
    regionId?: string;
    sourceService?: string;
    correlationId?: string;
    mapVersion?: string;
    eventTimestamp?: string;
    createdAt?: string;
    payloadJson?: string;
}
