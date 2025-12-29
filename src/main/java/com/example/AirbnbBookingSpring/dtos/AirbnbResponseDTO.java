package com.example.AirbnbBookingSpring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AirbnbResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerNight;
    private String cityName;

}
