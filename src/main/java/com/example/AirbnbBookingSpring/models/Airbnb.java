package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "airbnb")
public class Airbnb extends BaseModel {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String cityName; // i will figure it out about lat and long // todo

    @Column(nullable = false)
    private BigDecimal pricePerNight;

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
