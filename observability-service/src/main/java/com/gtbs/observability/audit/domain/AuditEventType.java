package com.gtbs.observability.audit.domain;

public enum AuditEventType {
    PENDING,
    RESERVED,
    CONFIRMED,
    REJECTED,
    CANCELLED,
    FAILED,
    POLICY_CHANGED,
    SEGMENT_CLOSED,
    SEGMENT_REOPENED
}
