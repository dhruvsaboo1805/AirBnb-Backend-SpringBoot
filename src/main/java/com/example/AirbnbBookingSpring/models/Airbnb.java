package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "airbnb")
@Data
public class Airbnb extends BaseModel {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String cityName; // i will figure it out about lat and long // todo

    @Column(nullable = false)
    private double pricePerNight;

    //Relationships

    // Airbnb user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Airbnb bookings
    @OneToMany(mappedBy = "airbnb", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    // Availability slots
    @OneToMany(mappedBy = "airbnb", cascade = CascadeType.ALL)
    private List<Availability> availabilityList;
}
