package com.rideshare.ride_service.service;

import com.rideshare.ride_service.event.RideMatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchedEventConsumer {

    private final RideService rideService;

    @KafkaListener(
            topics = "ride.matched",
            groupId = "ride-service-group"
    )
    public void consumeRideRequestedEvent(RideMatchedEvent event){
        try{
            rideService.updateRideWithDriver(event.getRideId(), event.getDriverId());
        }
        catch (Exception e){
            log.error("Error processing ride request: {} - {}",
                    event.getRideId(), e.getMessage());
        }
    }
}
