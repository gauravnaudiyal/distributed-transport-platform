package com.gtbs.adminauthority.integration.rms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RmsApplyResponse(Long mapVersion, String policyReference) {}
