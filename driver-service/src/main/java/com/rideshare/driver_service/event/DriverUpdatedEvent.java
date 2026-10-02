package com.rideshare.driver_service.event;


import com.rideshare.driver_service.model.DriverAvailability;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Primary;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverUpdatedEvent {

    private String id;
    private DriverAvailability availability;
}
