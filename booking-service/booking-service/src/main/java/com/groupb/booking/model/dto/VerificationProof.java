package com.groupb.booking.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class VerificationProof {

    @JsonProperty("booking_id")
    private String bookingId;

    @JsonProperty("proof_token")
    private String proofToken;

    @JsonProperty("map_version")
    private String mapVersion;

    @JsonProperty("time_window_start")
    private Instant timeWindowStart;

    @JsonProperty("time_window_end")
    private Instant timeWindowEnd;

    @JsonProperty("segment_scope")
    private List<String> segmentScope;

    public VerificationProof() {}

    public VerificationProof(String bookingId, String proofToken, String mapVersion,
                             Instant timeWindowStart, Instant timeWindowEnd, List<String> segmentScope) {
        this.bookingId = bookingId;
        this.proofToken = proofToken;
        this.mapVersion = mapVersion;
        this.timeWindowStart = timeWindowStart;
        this.timeWindowEnd = timeWindowEnd;
        this.segmentScope = segmentScope;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getProofToken() { return proofToken; }
    public void setProofToken(String proofToken) { this.proofToken = proofToken; }

    public String getMapVersion() { return mapVersion; }
    public void setMapVersion(String mapVersion) { this.mapVersion = mapVersion; }

    public Instant getTimeWindowStart() { return timeWindowStart; }
    public void setTimeWindowStart(Instant timeWindowStart) { this.timeWindowStart = timeWindowStart; }

    public Instant getTimeWindowEnd() { return timeWindowEnd; }
    public void setTimeWindowEnd(Instant timeWindowEnd) { this.timeWindowEnd = timeWindowEnd; }

    public List<String> getSegmentScope() { return segmentScope; }
    public void setSegmentScope(List<String> segmentScope) { this.segmentScope = segmentScope; }
}
