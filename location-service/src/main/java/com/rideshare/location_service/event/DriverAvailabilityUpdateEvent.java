package com.rideshare.location_service.event;


import com.rideshare.location_service.enums.DriverAvailability;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverAvailabilityUpdateEvent {

    private String id;
    private DriverAvailability availability;
    private Double latitude;
    private Double longitude;
}
