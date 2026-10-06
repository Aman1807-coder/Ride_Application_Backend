package com.rideshare.matching_service.repository;

import com.rideshare.matching_service.model.ProcessedRide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedRideRepository extends JpaRepository<ProcessedRide, String> {
}
