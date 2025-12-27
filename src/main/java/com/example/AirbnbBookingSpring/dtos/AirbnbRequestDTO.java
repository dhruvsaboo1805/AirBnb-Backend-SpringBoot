package com.example.AirbnbBookingSpring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AirbnbRequestDTO {
    private String name;
    private String description;
    private double pricePerNight;
    private String cityName;
}
