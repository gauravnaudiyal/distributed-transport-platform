package com.global_traffic_bs.journey_compatibility_svc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {

    @NotBlank(message = "bookingId is required")
    private String bookingId;

    @NotBlank(message = "routeId is required")
    private String routeId;

    @NotBlank(message = "routeName is required")
    private String routeName;

    @NotNull(message = "travelDate is required")
    @FutureOrPresent(message = "travelDate must be today or a future date")
    private LocalDate travelDate;

    @NotNull(message = "travelTime is required")
    private LocalTime travelTime;

    @NotBlank(message = "vehicleType is required")
    @Pattern(regexp = "CAR|BUS|TRUCK", message = "vehicleType must be CAR, BUS, or TRUCK")
    private String vehicleType;

    @NotNull(message = "passengerCount is required")
    @Min(value = 1, message = "passengerCount must be at least 1")
    @Max(value = 100, message = "passengerCount cannot exceed 100")
    private Integer passengerCount;

    @NotBlank(message = "driverId is required")
    private String driverId;
}
