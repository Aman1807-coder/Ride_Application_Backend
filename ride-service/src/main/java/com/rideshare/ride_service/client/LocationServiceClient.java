package com.rideshare.ride_service.client;

import com.rideshare.ride_service.dto.DriverDistanceToPickupRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "location-service", url = "${location.service.url}")
public interface LocationServiceClient {

    @PostMapping("/api/v1/locations/drivers/get_driver_distance")
    Double getDriverDistanceToPickup(@RequestBody DriverDistanceToPickupRequest request);
}
