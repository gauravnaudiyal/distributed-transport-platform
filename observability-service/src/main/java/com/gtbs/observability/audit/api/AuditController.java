package com.gtbs.observability.audit.api;

import com.gtbs.observability.audit.service.AuditLogService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/bookings/{bookingId}")
    public List<AuditRecordResponse> getByBookingId(@PathVariable UUID bookingId) {
        return auditLogService.findByBookingId(bookingId);
    }

    @GetMapping("/segments/{segmentId}")
    public List<AuditRecordResponse> getBySegmentId(@PathVariable String segmentId) {
        return auditLogService.findBySegmentId(segmentId);
    }
}
