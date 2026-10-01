package com.rideshare.matching_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response received from Location Service
 * When querying for nearby drivers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearByDriverResponse {

    private String driverId;
    private Double latitude;
    private Double longitude;
    private Double distanceInKm;
}
