package com.rideshare.location_service.controller;


import com.rideshare.location_service.dto.DriverDistanceToPickupRequest;
import com.rideshare.location_service.dto.DriverLocationRequest;
import com.rideshare.location_service.dto.NearByDriverResponse;
import com.rideshare.location_service.service.LocationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/locations")
@Slf4j
@AllArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/drivers/get_driver_distance")
    public ResponseEntity<Double> getDriverDistanceToPickup
            (@RequestBody DriverDistanceToPickupRequest request) {

        return ResponseEntity.ok(locationService.getDriverDistanceToPickup(request));
    }

    //driver's phone call this method every 3 seconds
    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation (
            @RequestBody DriverLocationRequest driverLocationRequest) {

        locationService.updateDriverLocation(driverLocationRequest);
        return ResponseEntity.ok("Driver location updated");
    }

    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearByDriverResponse>> getNearByDrivers (
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radius
    ) {

        return ResponseEntity.ok(locationService.findNearByDrivers(latitude, longitude, radius));
    }

    @DeleteMapping("/drivers/remove/{driverId}")
    public ResponseEntity<String> removeDrivers(@PathVariable String driverId) {

        locationService.removeDriver(driverId);
        return ResponseEntity.ok("driver removed successfully");
    }
}
