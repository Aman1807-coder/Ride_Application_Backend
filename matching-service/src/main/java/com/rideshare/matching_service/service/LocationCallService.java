package com.rideshare.matching_service.service;


import com.rideshare.matching_service.client.LocationServiceClient;
import com.rideshare.matching_service.dto.NearByDriverResponse;
import com.rideshare.matching_service.event.RideRequestedEvent;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Data
public class LocationCallService {

    private final LocationServiceClient locationServiceClient;

    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    @RateLimiter(
            name = "locationService",
            fallbackMethod = "locationServiceFallback"
    )
    public List<NearByDriverResponse> callGetNearByDrivers(
            RideRequestedEvent event) {

        log.info(
                ">>> Calling Location Service for ride {}",
                event.getRideId()
        );

        return locationServiceClient.getNearByDrivers(
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM
        );
    }

    public List<NearByDriverResponse> locationServiceFallback(
            RideRequestedEvent event,
            Throwable throwable) {

        log.warn(
                "Rate limit reached for ride {}. Error={}",
                event.getRideId(),
                throwable.getMessage()
        );

        return Collections.emptyList();
    }
}
