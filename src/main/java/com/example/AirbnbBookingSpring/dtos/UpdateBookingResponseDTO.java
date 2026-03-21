package com.example.AirbnbBookingSpring.dtos;

import com.example.AirbnbBookingSpring.models.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingResponseDTO {

    private Long id;
    private Long airbnbId;
    private String customerEmail;
    private BigDecimal totalPrice;
    private Booking.BookingStatus bookingStatus;
    private String idempotencyKey;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Instant updatedAt;

    public static UpdateBookingResponseDTO fromBooking(Booking booking) {
        return UpdateBookingResponseDTO.builder()
                .id(booking.getId())
                .airbnbId(booking.getAirbnb().getId())
                .customerEmail(booking.getCustomerEmail())
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus())
                .idempotencyKey(booking.getIdempotencyKey())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}