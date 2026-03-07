package com.example.AirbnbBookingSpring.exceptions;

public class BookingException extends RuntimeException {
    public BookingException(String message) {
        super(message);
    }
}