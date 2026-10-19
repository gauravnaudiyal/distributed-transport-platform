package com.gtbs.adminauthority.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtbs.adminauthority.api.dto.AppliedInstructionResponse;
import com.gtbs.adminauthority.api.dto.AuditEntryResponse;
import com.gtbs.adminauthority.api.dto.BookingVerificationResponse;
import com.gtbs.adminauthority.api.dto.CapacityOverrideRequest;
import com.gtbs.adminauthority.api.dto.ClosureRequest;
import com.gtbs.adminauthority.api.dto.RestrictionRequest;
import com.gtbs.adminauthority.api.dto.SegmentForecastResponse;
import com.gtbs.adminauthority.audit.AuthorityInstructionAuditEntity;
import com.gtbs.adminauthority.audit.AuthorityInstructionAuditRepository;
import com.gtbs.adminauthority.audit.InstructionStatus;
import com.gtbs.adminauthority.audit.InstructionType;
import com.gtbs.adminauthority.events.AuthorityEventPublisher;
import com.gtbs.adminauthority.integration.booking.BookingVerificationGateway;
import com.gtbs.adminauthority.integration.rms.RouteManagementException;
import com.gtbs.adminauthority.integration.rms.RouteManagementGateway;
import com.gtbs.adminauthority.integration.rms.RmsApplyResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class AuthorityInstructionService {

    private static final int AUDIT_PAGE_SIZE = 100;

    private final RouteManagementGateway routeManagementGateway;
    private final BookingVerificationGateway bookingVerificationGateway;
    private final AuthorityInstructionAuditRepository auditRepository;
    private final AuthorityEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public AuthorityInstructionService(
            RouteManagementGateway routeManagementGateway,
            BookingVerificationGateway bookingVerificationGateway,
            AuthorityInstructionAuditRepository auditRepository,
            AuthorityEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            TransactionTemplate transactionTemplate) {
        this.routeManagementGateway = routeManagementGateway;
        this.bookingVerificationGateway = bookingVerificationGateway;
        this.auditRepository = auditRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
    }

    public AppliedInstructionResponse submitClosure(ClosureRequest request) {
        return submit(InstructionType.CLOSURE, request, routeManagementGateway::applyClosure);
    }

    public AppliedInstructionResponse submitRestriction(RestrictionRequest request) {
        return submit(InstructionType.RESTRICTION, request, routeManagementGateway::applyRestriction);
    }

    public AppliedInstructionResponse submitCapacityOverride(CapacityOverrideRequest request) {
        return submit(InstructionType.CAPACITY_OVERRIDE, request, routeManagementGateway::applyCapacityOverride);
    }

    public BookingVerificationResponse verifyBooking(String bookingId) {
        return bookingVerificationGateway.verify(bookingId);
    }

    public SegmentForecastResponse segmentForecast(String segmentId) {
        return routeManagementGateway.getForecast(segmentId);
    }

    public List<AuditEntryResponse> auditTrailForSegment(String segmentId) {
        return auditRepository.findRecentForSegment(segmentId, AUDIT_PAGE_SIZE).stream()
                .map(this::toAuditDto)
                .toList();
    }

    private <T> AppliedInstructionResponse submit(
            InstructionType type, T request, RmsInvoker<T> rmsInvoker) {
        RmsApplyResponse rms;
        try {
            rms = rmsInvoker.invoke(request);
        } catch (RouteManagementException ex) {
            transactionTemplate.executeWithoutResult(status -> persistFailure(type, request, ex.getMessage()));
            throw ex;
        }
        return transactionTemplate.execute(status -> {
            persistApplied(type, request, rms);
            eventPublisher.publishedApplied(type, correlationIdFrom(request), rms.mapVersion());
            return new AppliedInstructionResponse(
                    rms.mapVersion(),
                    correlationIdFrom(request),
                    rms.policyReference() != null ? rms.policyReference() : "");
        });
    }

    private void persistApplied(InstructionType type, Object request, RmsApplyResponse rms) {
        AuthorityInstructionAuditEntity e = baseEntity(type, request, InstructionStatus.APPLIED);
        e.setMapVersion(rms.mapVersion());
        e.setExternalRef(rms.policyReference());
        auditRepository.save(e);
    }

    private void persistFailure(InstructionType type, Object request, String errorDetail) {
        AuthorityInstructionAuditEntity e = baseEntity(type, request, InstructionStatus.FAILED);
        e.setErrorDetail(errorDetail);
        auditRepository.save(e);
    }

    private AuthorityInstructionAuditEntity baseEntity(InstructionType type, Object request, InstructionStatus status) {
        AuthorityInstructionAuditEntity e = new AuthorityInstructionAuditEntity();
        e.setId(UUID.randomUUID());
        e.setInstructionType(type);
        e.setCorrelationId(correlationIdFrom(request));
        e.setJurisdictionId(jurisdictionIdFrom(request));
        e.setSegmentIds(segmentIdsFrom(request));
        try {
            e.setPayloadJson(objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException ex) {
            e.setPayloadJson("{\"error\":\"payload serialization failed\"}");
        }
        e.setStatus(status);
        return e;
    }

    private static String correlationIdFrom(Object request) {
        return switch (request) {
            case ClosureRequest r -> r.correlationId();
            case RestrictionRequest r -> r.correlationId();
            case CapacityOverrideRequest r -> r.correlationId();
            default -> throw new IllegalArgumentException("Unsupported request type");
        };
    }

    private static String jurisdictionIdFrom(Object request) {
        return switch (request) {
            case ClosureRequest r -> r.jurisdictionId();
            case RestrictionRequest r -> r.jurisdictionId();
            case CapacityOverrideRequest r -> r.jurisdictionId();
            default -> throw new IllegalArgumentException("Unsupported request type");
        };
    }

    private static List<String> segmentIdsFrom(Object request) {
        return switch (request) {
            case ClosureRequest r -> r.segmentIds();
            case RestrictionRequest r -> r.segmentIds();
            case CapacityOverrideRequest r -> r.segmentIds();
            default -> throw new IllegalArgumentException("Unsupported request type");
        };
    }

    private AuditEntryResponse toAuditDto(AuthorityInstructionAuditEntity e) {
        return new AuditEntryResponse(
                e.getId().toString(),
                e.getInstructionType(),
                e.getCorrelationId(),
                e.getSegmentIds() != null ? e.getSegmentIds() : List.of(),
                e.getMapVersion() != null ? e.getMapVersion() : 0L,
                e.getStatus(),
                e.getCreatedAt(),
                e.getExternalRef());
    }

    @FunctionalInterface
    private interface RmsInvoker<T> {
        RmsApplyResponse invoke(T request);
    }
}
