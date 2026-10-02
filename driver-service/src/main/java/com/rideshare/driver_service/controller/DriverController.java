package com.rideshare.driver_service.controller;

import com.rideshare.driver_service.dto.DriverAddRequest;
import com.rideshare.driver_service.dto.DriverResponse;
import com.rideshare.driver_service.dto.DriverUpdateRequest;
import com.rideshare.driver_service.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable String driverId) {

        return ResponseEntity.status(HttpStatus.OK).body(driverService.getDriverById(driverId));
    }

    @PostMapping("/add")
    public ResponseEntity<DriverResponse> addDriver(@RequestBody DriverAddRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.addDriver(request));
    }

    @PutMapping("/update/availability")
    public ResponseEntity<String> updateDriverAvailability(@RequestBody DriverUpdateRequest request) {

        driverService.updateAvailability(request);

        return ResponseEntity.ok().body("Driver availability updated successfully");
    }
}
