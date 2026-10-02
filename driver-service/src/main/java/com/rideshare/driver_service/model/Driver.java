package com.rideshare.driver_service.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drivers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String drivingLicenseId;

    @Enumerated(EnumType.STRING)
    private DriverAvailability availability;

    private Double rating;
}
