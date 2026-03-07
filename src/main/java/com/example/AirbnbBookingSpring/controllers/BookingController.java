package com.example.AirbnbBookingSpring.controllers;

import com.example.AirbnbBookingSpring.dtos.BookingRequestDTO;
import com.example.AirbnbBookingSpring.dtos.BookingResponseDTO;
import com.example.AirbnbBookingSpring.dtos.UpdateBookingRequestDTO;
import com.example.AirbnbBookingSpring.dtos.UpdateBookingResponseDTO;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.services.BookingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody BookingRequestDTO bookingRequestDTO) throws JsonProcessingException {
        Booking booking = bookingService.createBooking(bookingRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponseDTO.fromBooking(booking));
    }

    @PatchMapping
    public ResponseEntity<UpdateBookingResponseDTO> updateBooking(@RequestBody UpdateBookingRequestDTO updateBookingRequestDTO) {
        Booking booking = bookingService.updateBooking(updateBookingRequestDTO);
        return ResponseEntity.ok(UpdateBookingResponseDTO.fromBooking(booking));
    }
}