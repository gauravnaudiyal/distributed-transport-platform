package com.gtbs.adminauthority.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "authority_instruction_audit")
public class AuthorityInstructionAuditEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private InstructionType instructionType;

    @Column(nullable = false, length = 128)
    private String correlationId;

    @Column(length = 128)
    private String jurisdictionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> segmentIds;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    private Long mapVersion;

    @Column(length = 256)
    private String externalRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InstructionStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorDetail;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    public void setInstructionType(InstructionType instructionType) {
        this.instructionType = instructionType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public List<String> getSegmentIds() {
        return segmentIds;
    }

    public void setSegmentIds(List<String> segmentIds) {
        this.segmentIds = segmentIds;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public Long getMapVersion() {
        return mapVersion;
    }

    public void setMapVersion(Long mapVersion) {
        this.mapVersion = mapVersion;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }

    public InstructionStatus getStatus() {
        return status;
    }

    public void setStatus(InstructionStatus status) {
        this.status = status;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
