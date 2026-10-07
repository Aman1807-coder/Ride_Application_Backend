package com.rideshare.matching_service.service;


import com.rideshare.matching_service.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideEventConsumer {

    private final MatchingService matchingService;

    /**
     * Listens to ride.requested kafka topic.
     * Triggered every time Ride Service published a new ride request
     *
     * FLOW:
     * Ride Service -> Kafka (ride.requested) -> This Consumer -> MatchingService
     */

    @KafkaListener(
            topics = "ride.requested",
            groupId = "matching-service-group"
    )
    public void consumeRideRequestedEvent(RideRequestedEvent event) {
        matchingService.matchDriverForRide(event);
    }
}
