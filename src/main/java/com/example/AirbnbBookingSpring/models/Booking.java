package com.example.AirbnbBookingSpring.models;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "booking")
public class Booking extends BaseModel{

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @Column(unique = true)
    private String idempotencyKey;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    @Column(nullable = false)
    private String customerEmail;

    // ENUMS
    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED;
    }

    // RelationsShips

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="airbnb_id",nullable = false)
    private Airbnb airbnb;



}
