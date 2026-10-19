package com.groupb.booking.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JourneyCompatibilityRequest {

    @JsonProperty("bookingId")
    private String bookingId;

    @JsonProperty("routeId")
    private String routeId;

    @JsonProperty("routeName")
    private String routeName;

    @JsonProperty("travelDate")
    private String travelDate;

    @JsonProperty("travelTime")
    private String travelTime;

    @JsonProperty("vehicleType")
    private String vehicleType;

    @JsonProperty("passengerCount")
    private Integer passengerCount;

    @JsonProperty("driverId")
    private String driverId;

    public JourneyCompatibilityRequest() {}

    public JourneyCompatibilityRequest(String bookingId, String routeId, String routeName,
                                        String travelDate, String travelTime, String vehicleType,
                                        Integer passengerCount, String driverId) {
        this.bookingId = bookingId;
        this.routeId = routeId;
        this.routeName = routeName;
        this.travelDate = travelDate;
        this.travelTime = travelTime;
        this.vehicleType = vehicleType;
        this.passengerCount = passengerCount;
        this.driverId = driverId;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getTravelDate() { return travelDate; }
    public void setTravelDate(String travelDate) { this.travelDate = travelDate; }

    public String getTravelTime() { return travelTime; }
    public void setTravelTime(String travelTime) { this.travelTime = travelTime; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Integer getPassengerCount() { return passengerCount; }
    public void setPassengerCount(Integer passengerCount) { this.passengerCount = passengerCount; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
}

