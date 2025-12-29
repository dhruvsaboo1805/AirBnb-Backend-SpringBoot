package com.example.AirbnbBookingSpring.models.readModels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingReadModel {

    private Long id;

    private Long airbnbId;

    private Long userId;

    private BigDecimal totalPrice;

    private String bookingStatus;

    private String idempotencyKey;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;
    
}
