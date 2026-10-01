package com.rideshare.ride_service.dto;


import com.rideshare.ride_service.model.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchedRideResponse {

    private String id;

    private String riderId;
    private String driverId;

    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;

    private Double dropLatitude;
    private Double dropLongitude;
    private String dropAddress;

    private Double distanceToPickup;

    private RideStatus rideStatus;

    private Double estimatedFare;
    private Double actualFare;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
