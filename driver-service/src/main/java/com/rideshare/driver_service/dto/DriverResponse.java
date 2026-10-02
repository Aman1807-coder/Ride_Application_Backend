package com.rideshare.driver_service.dto;


import com.rideshare.driver_service.model.DriverAvailability;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverResponse {

    private String id;
    private String name;
    private String vehicleNumber;
    private String phoneNumber;
    private DriverAvailability availability;
    private Double rating;
}
