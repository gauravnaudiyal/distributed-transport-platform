package com.groupb.booking.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class RouteDecomposition {

    @JsonProperty("map_version")
    private String mapVersion;

    private List<Segment> segments;

    public RouteDecomposition() {}

    public RouteDecomposition(String mapVersion, List<Segment> segments) {
        this.mapVersion = mapVersion;
        this.segments = segments;
    }

    public String getMapVersion() { return mapVersion; }
    public void setMapVersion(String mapVersion) { this.mapVersion = mapVersion; }

    public List<Segment> getSegments() { return segments; }
    public void setSegments(List<Segment> segments) { this.segments = segments; }

    public static class Segment {

        @JsonProperty("segment_id")
        private String segmentId;

        @JsonProperty("authoritative_region")
        private String authoritativeRegion;

        @JsonProperty("time_window_start")
        private Instant timeWindowStart;

        @JsonProperty("time_window_end")
        private Instant timeWindowEnd;

        public Segment() {}

        public Segment(String segmentId, String authoritativeRegion,
                       Instant timeWindowStart, Instant timeWindowEnd) {
            this.segmentId = segmentId;
            this.authoritativeRegion = authoritativeRegion;
            this.timeWindowStart = timeWindowStart;
            this.timeWindowEnd = timeWindowEnd;
        }

        public String getSegmentId() { return segmentId; }
        public void setSegmentId(String segmentId) { this.segmentId = segmentId; }

        public String getAuthoritativeRegion() { return authoritativeRegion; }
        public void setAuthoritativeRegion(String authoritativeRegion) { this.authoritativeRegion = authoritativeRegion; }

        public Instant getTimeWindowStart() { return timeWindowStart; }
        public void setTimeWindowStart(Instant timeWindowStart) { this.timeWindowStart = timeWindowStart; }

        public Instant getTimeWindowEnd() { return timeWindowEnd; }
        public void setTimeWindowEnd(Instant timeWindowEnd) { this.timeWindowEnd = timeWindowEnd; }
    }
}
