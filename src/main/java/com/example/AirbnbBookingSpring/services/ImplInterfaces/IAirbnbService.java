package com.example.AirbnbBookingSpring.services.ImplInterfaces;

import com.example.AirbnbBookingSpring.dtos.AirbnbRequestDTO;
import com.example.AirbnbBookingSpring.dtos.AirbnbResponseDTO;

import java.util.List;
import java.util.Optional;

public interface IAirbnbService {
    public AirbnbResponseDTO createAirbnb(AirbnbRequestDTO AirbnbRequestDTO);
    public Optional<AirbnbResponseDTO> getAirbnbById(Long id);
    public List<AirbnbResponseDTO> getAllAirbnb();
    public AirbnbResponseDTO updateAirbnb(AirbnbRequestDTO AirbnbRequestDTO);
    public boolean deleteAirbnb(Long id);
}
