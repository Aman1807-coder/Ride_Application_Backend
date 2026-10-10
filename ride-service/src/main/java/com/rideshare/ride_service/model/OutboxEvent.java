package com.rideshare.ride_service.model;


import com.rideshare.ride_service.event.RideRequestedEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Boolean published;
}
