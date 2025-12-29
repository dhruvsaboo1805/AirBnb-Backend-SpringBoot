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

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private long id;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "airbnb_id", insertable = false, updatable = false)
    private Long airbnbId;

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
