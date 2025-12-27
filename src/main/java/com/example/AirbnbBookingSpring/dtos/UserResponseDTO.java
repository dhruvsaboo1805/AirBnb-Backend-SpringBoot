package com.example.AirbnbBookingSpring.dtos;

import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private List<Airbnb> airbnbs;
    private List<Booking> bookings;
}
