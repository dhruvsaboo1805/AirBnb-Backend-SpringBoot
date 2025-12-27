package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.dtos.AirbnbRequestDTO;
import com.example.AirbnbBookingSpring.dtos.AirbnbResponseDTO;
import com.example.AirbnbBookingSpring.mappers.AirbnbMapper;
import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.repositories.writes.AirbnbWriteRepository;
import com.example.AirbnbBookingSpring.services.ImplInterfaces.IAirbnbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AirbnbService implements IAirbnbService {

    private final AirbnbWriteRepository airbnbWriteRepository;

    @Override
    public AirbnbResponseDTO createAirbnb(AirbnbRequestDTO AirbnbRequestDTO) {
        Airbnb airbnb = AirbnbMapper.toEntity(AirbnbRequestDTO);
        airbnbWriteRepository.save(airbnb);
        return AirbnbMapper.toDTO(airbnb);
    }

    @Override
    public Optional<AirbnbResponseDTO> getAirbnbById(Long id) {
        return airbnbWriteRepository.findById(id)
                .map(AirbnbMapper::toDTO);
    }

    @Override
    public List<AirbnbResponseDTO> getAllAirbnb() {
        List<Airbnb> airbnbs = airbnbWriteRepository.findAll();
        return airbnbs.stream()
                .map(AirbnbMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AirbnbResponseDTO updateAirbnb(AirbnbRequestDTO AirbnbRequestDTO) {
        Airbnb airbnb = AirbnbMapper.toEntity(AirbnbRequestDTO);
        airbnbWriteRepository.save(airbnb);
        return AirbnbMapper.toDTO(airbnb);
    }

    @Override
    public boolean deleteAirbnb(Long id) {
        if (!airbnbWriteRepository.existsById(id)) {
            return false;
        }
        airbnbWriteRepository.deleteById(id);
        return true;
    }

}
