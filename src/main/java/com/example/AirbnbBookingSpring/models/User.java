package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "user")
public class User extends BaseModel {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String password; // for dev mode its nullable true but for it will make nullable false //todo

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Airbnb> airbnbList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Booking> bookingList;

}
