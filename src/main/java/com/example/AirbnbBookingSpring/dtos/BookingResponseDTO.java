package com.example.AirbnbBookingSpring.dtos;

import com.example.AirbnbBookingSpring.models.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {

    private Long id;
    private Long airbnbId;
    private Long userId;
    private BigDecimal totalPrice;
    private Booking.BookingStatus bookingStatus;
    private String idempotencyKey;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public static BookingResponseDTO fromBooking(Booking booking) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .airbnbId(booking.getAirbnb().getId())
                .userId(booking.getUser().getId())
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus())
                .idempotencyKey(booking.getIdempotencyKey())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .build();
    }
}