package com.rideshare.driver_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverAddRequest {

    private String name;
    private String vehicleNumber;
    private String phoneNumber;
    private String drivingLicenseId;
}
