package com.example.AirbnbBookingSpring.models.readModels;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AirbnbReadModel {

    private Long id;

    private String name;

    private String description;

    private String location;

    private BigDecimal pricePerNight;

    private List<AvailabilityReadModel> availability;
    
}
