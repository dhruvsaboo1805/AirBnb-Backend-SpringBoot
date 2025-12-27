package com.example.AirbnbBookingSpring.mappers;

import com.example.AirbnbBookingSpring.dtos.BookingRequestDTO;
import com.example.AirbnbBookingSpring.models.Booking;

public class BookingMapper {
    public static Booking toEntity(BookingRequestDTO bookingRequestDTO) {
        return Booking.builder()
                .userId(bookingRequestDTO.getUserId())
                .airbnbId(bookingRequestDTO.getAirbnbId())
                .checkInDate(bookingRequestDTO.getCheckInDate())
                .checkOutDate(bookingRequestDTO.getCheckOutDate())
                .build();
    }

}
