package com.rideshare.matching_service.service;


import com.rideshare.matching_service.client.LocationServiceClient;
import com.rideshare.matching_service.dto.NearByDriverResponse;
import com.rideshare.matching_service.event.RideMatchedEvent;
import com.rideshare.matching_service.event.RideRequestedEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {

    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    /**
     * Driver Scoring algorithm
     *
     * Distance: 70%
     * Rating: 30%
     *
     * Score = (1 / distance) * distanceWeight + rating * ratingWeight
     *
     * @param drivers
     * @return
     */


    private Optional<NearByDriverResponse> findBestDriver(
            List<NearByDriverResponse> drivers){

        double distanceWeight = 0.7;
        double ratingWeight = 0.3;

        return drivers.stream()
                .max(Comparator.comparingDouble(driver -> {
                    //Distance score: closer = higher score
                    // Add 0.1 to avoid division by zero
                    double distanceScore = 1.0/(driver.getDistanceInKm() + 0.1);

                    // Simulated rating between 4.0 and 5.0
                    // In production: fetch from Driver Service

                    double simulatedRating = 4.0 + Math.random();

                    //Final weighted score
                    return (distanceScore * distanceWeight)
                            + (simulatedRating * ratingWeight);
                }));
    }

    /**
     * Main matching alogorith
     * Called when RideRequestedEvent is consumed from Kafka
     * @param event
     *
     * STEPS:
     * 1. Ask Location Service for nearby drivers
     * 2. Score each driver and pick the best one
     */

    @Retry(name = "locationService", fallbackMethod = "locationServiceFallback")
    @CircuitBreaker(name = "locationService")
    public List<NearByDriverResponse> callGetNearByDrivers(RideRequestedEvent event) {

        List<NearByDriverResponse> nearByDrivers = locationServiceClient.getNearByDrivers(
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM
        );

        return nearByDrivers;
    }


    public void matchDriverForRide(RideRequestedEvent event) {

        List<NearByDriverResponse> nearByDrivers = callGetNearByDrivers(event);

        if (nearByDrivers.isEmpty()){
            log.warn("No drivers found near ride");
            return;
        }

        // STEP 2: Score each driver and pick the best one
        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearByDrivers);

        if (bestDriver.isEmpty()){
            log.warn("could not find suitable driver for ride");
            return;
        }

        NearByDriverResponse assignedDriver = bestDriver.get();

        //STEP 3: Publish RideMatchedEvent to Kafka
        RideMatchedEvent matchedEvent = new RideMatchedEvent (
                event.getRideId(),
                event.getRiderId(),
                assignedDriver.getDriverId(),
                assignedDriver.getLatitude(),
                assignedDriver.getLongitude(),
                assignedDriver.getDistanceInKm()
        );

        kafkaTemplate.send(RIDE_MATCHED_TOPIC, event.getRideId(), matchedEvent);
        log.info("RideMatchedEvent published with driver id {}", matchedEvent.getRideId());
    }

    public void locationServiceFallback (
            RideRequestedEvent event,
            Throwable throwable) {

        log.error(
                "Location Service unavailable. " +
                        "Circuit breaker fallback triggered. lat={}, lon={}, error={}",
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                throwable.getMessage()
        );
    }
}
