package com.example.AirbnbBookingSpring.mappers;

import com.example.AirbnbBookingSpring.dtos.AirbnbRequestDTO;
import com.example.AirbnbBookingSpring.dtos.AirbnbResponseDTO;
import com.example.AirbnbBookingSpring.models.Airbnb;

public class AirbnbMapper {
    public static Airbnb toEntity(AirbnbRequestDTO airbnbRequestDTO) {
        return Airbnb.builder()
                .name(airbnbRequestDTO.getName())
                .description(airbnbRequestDTO.getDescription())
                .cityName(airbnbRequestDTO.getCityName())
                .pricePerNight(airbnbRequestDTO.getPricePerNight())
                .build();
    }

    public static AirbnbResponseDTO toDTO(Airbnb airbnb) {
        return AirbnbResponseDTO.builder()
                .id(airbnb.getId())
                .name(airbnb.getName())
                .description(airbnb.getDescription())
                .cityName(airbnb.getCityName())
                .pricePerNight(airbnb.getPricePerNight())
                .build();
    }

}
