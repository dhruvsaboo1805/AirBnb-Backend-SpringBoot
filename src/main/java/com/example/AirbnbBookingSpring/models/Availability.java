package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Auditable;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "availability")
public class Availability extends BaseModel{

//    @Column(nullable = false)
//    private String airbnbId;

    @Column(nullable = false)
    private String date;
    
    private Long bookingId; // null if available

    // RelationShips

    // Which Airbnb
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airbnb_id", nullable = false)
    private Airbnb airbnb;

//    // Optional booking
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "booking_id")
//    private Booking booking;
}
