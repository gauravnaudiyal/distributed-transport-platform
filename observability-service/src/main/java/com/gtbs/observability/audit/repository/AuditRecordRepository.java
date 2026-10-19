package com.gtbs.observability.audit.repository;

import com.gtbs.observability.audit.domain.AuditRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRecordRepository extends JpaRepository<AuditRecord, UUID> {

    List<AuditRecord> findByBookingIdOrderByEventTimestampAsc(UUID bookingId);

    List<AuditRecord> findBySegmentIdOrderByEventTimestampAsc(String segmentId);
}
