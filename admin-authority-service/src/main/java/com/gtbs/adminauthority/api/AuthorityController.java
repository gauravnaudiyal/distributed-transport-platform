package com.gtbs.adminauthority.api;

import com.gtbs.adminauthority.api.dto.AppliedInstructionResponse;
import com.gtbs.adminauthority.api.dto.AuditEntryResponse;
import com.gtbs.adminauthority.api.dto.BookingVerificationResponse;
import com.gtbs.adminauthority.api.dto.CapacityOverrideRequest;
import com.gtbs.adminauthority.api.dto.ClosureRequest;
import com.gtbs.adminauthority.api.dto.RestrictionRequest;
import com.gtbs.adminauthority.api.dto.SegmentForecastResponse;
import com.gtbs.adminauthority.service.AuthorityInstructionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authority")
@Validated
@Tag(name = "Traffic Authority", description = "GTBS Traffic Authority Interface (instruction intake, verification, audit)")
public class AuthorityController {

    private final AuthorityInstructionService authorityInstructionService;

    public AuthorityController(AuthorityInstructionService authorityInstructionService) {
        this.authorityInstructionService = authorityInstructionService;
    }

    @PostMapping("/closures")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a temporary road closure (applied via Route Management)")
    public AppliedInstructionResponse closures(@Valid @RequestBody ClosureRequest request) {
        return authorityInstructionService.submitClosure(request);
    }

    @PostMapping("/restrictions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a route restriction (applied via Route Management)")
    public AppliedInstructionResponse restrictions(@Valid @RequestBody RestrictionRequest request) {
        return authorityInstructionService.submitRestriction(request);
    }

    @PostMapping("/capacity-overrides")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Override segment capacity for a window (applied via Route Management)")
    public AppliedInstructionResponse capacityOverrides(@Valid @RequestBody CapacityOverrideRequest request) {
        return authorityInstructionService.submitCapacityOverride(request);
    }

    @GetMapping("/bookings/{bookingId}/verify")
    @Operation(summary = "Verify booking proof for enforcement (proxied to Booking Service)")
    public BookingVerificationResponse verifyBooking(@PathVariable String bookingId) {
        return authorityInstructionService.verifyBooking(bookingId);
    }

    @GetMapping("/segments/{segmentId}/forecast")
    @Operation(summary = "Read utilization forecast for a segment (proxied to Route Management)")
    public SegmentForecastResponse forecast(@PathVariable String segmentId) {
        return authorityInstructionService.segmentForecast(segmentId);
    }

    @GetMapping("/audit/{segmentId}")
    @Operation(summary = "Recent authority instructions affecting a segment (local audit log)")
    public List<AuditEntryResponse> audit(@PathVariable String segmentId) {
        return authorityInstructionService.auditTrailForSegment(segmentId);
    }
}
