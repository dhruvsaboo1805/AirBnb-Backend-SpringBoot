package com.example.AirbnbBookingSpring.services.ImplInterfaces;

import com.example.AirbnbBookingSpring.dtos.BookingRequestDTO;
import com.example.AirbnbBookingSpring.dtos.UpdateBookingRequestDTO;
import com.example.AirbnbBookingSpring.models.Booking;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IBookingService {
    Booking createBooking(BookingRequestDTO createBookingRequest) throws JsonProcessingException;

    Booking updateBooking(UpdateBookingRequestDTO updateBookingRequest);
}
