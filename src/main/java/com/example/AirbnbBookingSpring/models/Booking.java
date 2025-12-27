package com.example.AirbnbBookingSpring.models;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "booking")
@Data
public class Booking extends BaseModel {

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "airbnb_id", insertable = false, updatable = false)
    private Long airbnbId;

    @Column(nullable = false)
    private String totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @Column(unique = true)
    private String idempotencyKey;

    // ENUMS
    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED;
    }

    // RelationsShips

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="airbnb_id",nullable = false)
    private Airbnb airbnb;



}
