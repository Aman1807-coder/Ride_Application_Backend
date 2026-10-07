package com.rideshare.matching_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_rides")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedRide {

    @Id
    private String rideId;

    private Instant processedAt;
}
