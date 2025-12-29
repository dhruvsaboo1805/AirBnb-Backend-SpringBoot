package com.example.AirbnbBookingSpring.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Auditable;

@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "availability")
public class Availability extends BaseModel{

    @Column(name = "airbnb_id", insertable = false, updatable = false)
    private Long airbnbId;

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
