package com.rideshare.location_service.service;

import com.rideshare.location_service.dto.DriverDistanceToPickupRequest;
import com.rideshare.location_service.dto.DriverLocationRequest;
import com.rideshare.location_service.dto.NearByDriverResponse;
import com.rideshare.location_service.exception.DriverNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    private static final String DRIVER_GEO_KEY = "drivers:locations";
    private final RedisTemplate<String, String> redisTemplate;

    private Double calculateDistance (
            Double lat1,
            Double lon1,
            Double lat2,
            Double lon2) {

        Double earthRadiusKm = 6371.0;

        Double dLat = Math.toRadians(lat2 - lat1);
        Double dLon = Math.toRadians(lon2 - lon1);

        Double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    public void updateDriverLocation(DriverLocationRequest driverLocationRequest) {
        log.info("Updating location for driver : {}", driverLocationRequest.getDriverId());

        Point driverPoint = new Point(
                driverLocationRequest.getLongitude(),
                driverLocationRequest.getLatitude()
        );

        redisTemplate.opsForGeo().add(
                DRIVER_GEO_KEY,
                driverPoint,
                driverLocationRequest.getDriverId()
        );

        log.info("Location updated successfully for driver : {}", driverLocationRequest.getDriverId());
    }

    public List<NearByDriverResponse> findNearByDrivers(
            Double latitude, Double longitude, Double radius) {

        log.info("Finding drivers near lat : {} long : {} within {} KM", latitude, longitude, radius);

        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radius, Metrics.KILOMETERS)
        );

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        DRIVER_GEO_KEY,
                        searchArea,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                .includeCoordinates()
                                .includeDistance()
                                .sortAscending()
                                .limit(10)
                );

        List<NearByDriverResponse> nearByDrivers = new ArrayList<>();

        if (results != null) {
            results.getContent().forEach(result -> {
                        RedisGeoCommands.GeoLocation<String> location = result.getContent();

                        nearByDrivers.add(new NearByDriverResponse(
                                        location.getName(),
                                        location.getPoint().getY(),
                                        location.getPoint().getX(),
                                        result.getDistance().getValue()
                                )
                        );
                    }
            );
        }

        log.info("Found {} drivers nearby ", nearByDrivers.size());

        return nearByDrivers;
    }

    public void removeDriver(String driverId) {
        log.info("Removing driver {}", driverId);
        redisTemplate.opsForGeo().remove(DRIVER_GEO_KEY, driverId);
    }

    public Double getDriverDistanceToPickup(DriverDistanceToPickupRequest request) {

        String driverId = request.getDriverId();
        Double pickupLongitude = request.getPickupLongitude();
        Double pickupLatitude = request.getPickupLatitude();

        List<Point> points = redisTemplate.opsForGeo()
                .position(DRIVER_GEO_KEY, driverId);

        if (points == null || points.isEmpty()) {
            throw new DriverNotFoundException("Driver with driver id " + driverId + " not found" );
        }

        Point driverPoint = points.get(0);

        Double driverLongitude = driverPoint.getX();
        Double driverLatitude = driverPoint.getY();
        System.out.println("driverId " + driverId);
        System.out.println("driver lon " + driverLongitude);
        System.out.println("driver lat " + driverLatitude);
        return calculateDistance(pickupLatitude, pickupLongitude, driverLatitude, driverLongitude);
    }
}
