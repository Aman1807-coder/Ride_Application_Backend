package com.rideshare.driver_service.service;

import com.rideshare.driver_service.dto.DriverAddRequest;
import com.rideshare.driver_service.dto.DriverResponse;
import com.rideshare.driver_service.dto.DriverUpdateRequest;
import com.rideshare.driver_service.event.DriverUpdatedEvent;
import com.rideshare.driver_service.model.Driver;
import com.rideshare.driver_service.model.DriverAvailability;
import com.rideshare.driver_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final KafkaTemplate<String, DriverUpdateRequest> kafkaTemplate;
    private static final String DRIVER_AVAILABILITY_UPDATED_TOPIC = "driver.availability_updated";

    public DriverResponse addDriver(DriverAddRequest request) {

        Driver driver = new Driver();

        driver.setName(request.getName());
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setVehicleNumber(request.getVehicleNumber());
        driver.setDrivingLicenseId(request.getDrivingLicenseId());
        driver.setRating(5.0);
        driver.setAvailability(DriverAvailability.OFFLINE);

        driverRepository.save(driver);

        return new DriverResponse(
                driver.getId(),
                driver.getName(),
                driver.getVehicleNumber(),
                driver.getPhoneNumber(),
                driver.getAvailability(),
                driver.getRating()
        );
    }

    public void updateAvailability(DriverUpdateRequest request) {

        Driver driver = driverRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));


        if (request.getAvailability().name().equals(driver.getAvailability().name())) {
            return;
        }

        driver.setAvailability(request.getAvailability());
        driverRepository.save(driver);

        kafkaTemplate.send(DRIVER_AVAILABILITY_UPDATED_TOPIC, request.getId(), request);
    }

    public DriverResponse getDriverById(String driverId) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        return new DriverResponse(
                driver.getId(),
                driver.getName(),
                driver.getVehicleNumber(),
                driver.getPhoneNumber(),
                driver.getAvailability(),
                driver.getRating()
        );
    }
}
