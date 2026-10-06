package com.rideshare.ride_service.service;

import com.rideshare.ride_service.client.LocationServiceClient;
import com.rideshare.ride_service.dto.DriverDistanceToPickupRequest;
import com.rideshare.ride_service.dto.MatchedRideResponse;
import com.rideshare.ride_service.dto.RideRequest;
import com.rideshare.ride_service.dto.RideResponse;
import com.rideshare.ride_service.event.RideRequestedEvent;
import com.rideshare.ride_service.model.Ride;
import com.rideshare.ride_service.model.RideStatus;
import com.rideshare.ride_service.repository.RideRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;
    private final LocationServiceClient locationServiceClient;
    private static final String RIDE_REQUESTED_TOPIC = "ride.requested";

    private MatchedRideResponse mapToMatchedRideResponse(Ride ride, Double driverDistanceToPickup) {

        MatchedRideResponse response = new MatchedRideResponse();

        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());
        response.setRideStatus((ride.getRideStatus()));
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setCreatedAt(ride.getCreatedAt());
        response.setDistanceToPickup(driverDistanceToPickup);

        return response;
    }

    private RideResponse mapToResponse(Ride ride) {

        RideResponse response = new RideResponse();

        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());
        response.setPickupAddress(ride.getPickupAddress());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());
        response.setDropAddress(ride.getDropAddress());
        response.setRideStatus((ride.getRideStatus()));
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setCreatedAt(ride.getCreatedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());

        return response;
    }

    private Double calculateEstimateFare(RideRequest request) {
        // Simplified Haversine distance calculation
        Double latFrom = Math.toRadians(request.getPickupLatitude());
        Double latTo = Math.toRadians(request.getDropLatitude());

        Double lonFrom = Math.toRadians(request.getPickupLongitude());
        Double lonTo = Math.toRadians(request.getDropLongitude());

        Double dLat = latTo - latFrom;
        Double dLon = lonTo - lonFrom;

        Double a =Math.pow(Math.sin(dLat / 2), 2)
                +Math.cos(latFrom) * Math.cos(latTo)
                *Math.pow(Math.sin(dLon / 2), 2);

        Double c = 2 * Math.asin(Math.sqrt(a));
        Double distanceKm = 6371 * c;

        //Base fare: 50Rs + 12Rs. perKm
        Double fare = 50 + (distanceKm * 12);
        return Math.round(fare * 100.0) / 100.0;
    }

    public RideResponse requestRide (@Valid RideRequest rideRequest) {

        log.info("new ride requested from rider: {}", rideRequest.getRiderId());

        // 1. save ride to database
        Ride ride = new Ride();
        ride.setRiderId(rideRequest.getRiderId());
        ride.setPickupLatitude(rideRequest.getPickupLatitude());
        ride.setPickupLongitude(rideRequest.getPickupLongitude());
        ride.setPickupAddress(rideRequest.getPickupAddress());
        ride.setDropLongitude(rideRequest.getDropLongitude());
        ride.setDropLatitude(rideRequest.getDropLatitude());
        ride.setDropAddress(rideRequest.getDropAddress());
        ride.setRideStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimateFare(rideRequest));

        Ride savedRide = rideRepository.save(ride);

        //2. publish event to kafka
        RideRequestedEvent event = new RideRequestedEvent(
                savedRide.getId(),
                savedRide.getRiderId(),
                savedRide.getPickupLatitude(),
                savedRide.getPickupLongitude(),
                savedRide.getPickupAddress(),
                savedRide.getDropLatitude(),
                savedRide.getDropLongitude(),
                savedRide.getDropAddress()
        );

        kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedRide.getId(), event);
        log.info("RideRequestedEvent published to Kafka for ride: {}", savedRide.getId());

        //Update status to Matching
        savedRide.setRideStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);

        return mapToResponse(savedRide);
    }

    public void updateRideWithDriver(String rideId, String driverId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setDriverId(driverId);
        ride.setRideStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);
    }

    public MatchedRideResponse getRideById(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        Double driverDistanceToPickup = null;

        try {
            driverDistanceToPickup = locationServiceClient.getDriverDistanceToPickup(
                    new DriverDistanceToPickupRequest(ride.getDriverId(), ride.getPickupLongitude(), ride.getPickupLatitude()));
        } catch(Exception e) {
            log.warn("Could not fetch driver distance for ride {}. Returning ride without distance.",
                    rideId, e);
        }

        return mapToMatchedRideResponse(ride, driverDistanceToPickup);
    }

    public List<RideResponse> getRidesByRider(String riderId) {
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public RideResponse startRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if(ride.getRideStatus() != RideStatus.ACCEPTED){
            throw new RuntimeException("Ride cannot be started. Current status: "+ride.getRideStatus());
        }

        ride.setRideStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        rideRepository.save(ride);

        return mapToResponse(ride);
    }

    public RideResponse completeRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getRideStatus() != RideStatus.RIDE_STARTED){
            throw new RuntimeException("Ride cannot be completed. Current status: "+ride.getRideStatus());
        }

        ride.setRideStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        rideRepository.save(ride);

        return mapToResponse(ride);

    }

    public RideResponse cancelRide(String rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setRideStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);
        return mapToResponse(ride);
    }
}
