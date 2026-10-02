package com.rideshare.driver_service.dto;


import com.rideshare.driver_service.model.DriverAvailability;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverUpdateRequest {

    private String id;
    private DriverAvailability availability;
    private Double latitude;
    private Double longitude;
}
