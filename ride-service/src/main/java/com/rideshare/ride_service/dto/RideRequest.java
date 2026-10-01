package com.rideshare.ride_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequest {

    private String riderId;

    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;

    private Double dropLatitude;
    private Double dropLongitude;
    private String dropAddress;

}
