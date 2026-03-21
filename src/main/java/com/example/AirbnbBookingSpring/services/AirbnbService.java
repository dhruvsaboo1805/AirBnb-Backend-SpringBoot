package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.dtos.AirbnbRequestDTO;
import com.example.AirbnbBookingSpring.dtos.AirbnbResponseDTO;
import com.example.AirbnbBookingSpring.mappers.AirbnbMapper;
import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.Availability;
import com.example.AirbnbBookingSpring.repositories.writes.AirbnbWriteRepository;
import com.example.AirbnbBookingSpring.repositories.writes.AvailabilityWriteRepository;
import com.example.AirbnbBookingSpring.services.ImplInterfaces.IAirbnbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AirbnbService implements IAirbnbService {

    private final AirbnbWriteRepository airbnbWriteRepository;
    private final AvailabilityWriteRepository availabilityWriteRepository;

    @Override
    public AirbnbResponseDTO createAirbnb(AirbnbRequestDTO airbnbRequestDTO , String ownerEmail) {
        log.info("[createAirbnb] START - ownerEmail={}, name={}, city={}",
                ownerEmail, airbnbRequestDTO.getName(), airbnbRequestDTO.getCityName());

        Airbnb airbnb = AirbnbMapper.toEntity(airbnbRequestDTO);
        airbnb.setOwnerEmail(ownerEmail);

        airbnbWriteRepository.save(airbnb);
        log.info("[createAirbnb] Airbnb saved to DB - airbnbId={}", airbnb.getId());

        seedAvailability(airbnb);

        log.info("[createAirbnb] END - airbnbId={} created successfully for ownerEmail={}",
                airbnb.getId(), ownerEmail);
        return AirbnbMapper.toDTO(airbnb);
    }

    @Override
    public Optional<AirbnbResponseDTO> getAirbnbById(Long id) {
        log.debug("[getAirbnbById] Fetching airbnb - id={}", id);

        Optional<AirbnbResponseDTO> result = airbnbWriteRepository.findById(id)
                .map(AirbnbMapper::toDTO);

        if (result.isEmpty()) {
            log.warn("[getAirbnbById] Airbnb not found - id={}", id);
        } else {
            log.debug("[getAirbnbById] Airbnb found - id={}", id);
        }

        return result;
    }

    @Override
    public List<AirbnbResponseDTO> getAllAirbnb() {
        log.debug("[getAllAirbnb] Fetching all airbnbs");
        List<Airbnb> airbnbs = airbnbWriteRepository.findAll();
        log.info("[getAllAirbnb] Found {} airbnbs", airbnbs.size());
        return airbnbs.stream()
                .map(AirbnbMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AirbnbResponseDTO updateAirbnb(AirbnbRequestDTO airbnbRequestDTO) {
        log.info("[updateAirbnb] START - name={}", airbnbRequestDTO.getName());

        Airbnb airbnb = AirbnbMapper.toEntity(airbnbRequestDTO);
        airbnbWriteRepository.save(airbnb);

        log.info("[updateAirbnb] END - airbnbId={} updated successfully", airbnb.getId());
        return AirbnbMapper.toDTO(airbnb);
    }

    @Override
    public boolean deleteAirbnb(Long id) {
        log.info("[deleteAirbnb] START - id={}", id);

        if (!airbnbWriteRepository.existsById(id)) {
            log.warn("[deleteAirbnb] Airbnb not found - id={}", id);
            return false;
        }

        airbnbWriteRepository.deleteById(id);
        log.info("[deleteAirbnb] END - airbnbId={} deleted successfully", id);
        return true;
    }

    private void seedAvailability(Airbnb airbnb) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(365);

        log.debug("[seedAvailability] Seeding availability for airbnbId={} from {} to {}",
                airbnb.getId(), today, end);

        List<Availability> slots = new ArrayList<>();
        for (LocalDate date = today; date.isBefore(end); date = date.plusDays(1)) {
            slots.add(Availability.builder()
                    .airbnb(airbnb)
                    .date(date)
                    .bookingId(null)
                    .build());
        }

        availabilityWriteRepository.saveAll(slots);
        log.info("[seedAvailability] Seeded {} availability slots for airbnbId={}", slots.size(), airbnb.getId());
    }
}