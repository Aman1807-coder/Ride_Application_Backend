package com.rideshare.location_service.service;


import com.rideshare.location_service.dto.DriverLocationRequest;
import com.rideshare.location_service.event.DriverAvailabilityUpdateEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor
public class DriverAvailabilityUpdateConsumer {

    private final LocationService locationService;

    @KafkaListener(
            topics = "driver.availability_updated",
            groupId = "driver-service-group"
    )
    public void consumeDriverAvailabilityUpdateEvent(DriverAvailabilityUpdateEvent event){

        if (event.getAvailability().name().equalsIgnoreCase("ONLINE")) {

            locationService.updateDriverLocation(new DriverLocationRequest(
                    event.getId(),
                    event.getLongitude(),
                    event.getLatitude()
            ));
        }

        else {
            locationService.removeDriver(event.getId());
        }
    }
}
