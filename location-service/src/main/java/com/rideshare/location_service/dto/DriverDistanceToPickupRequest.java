package com.rideshare.location_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverDistanceToPickupRequest {
    private String driverId;
    private Double pickupLongitude;
    private Double pickupLatitude;
}
