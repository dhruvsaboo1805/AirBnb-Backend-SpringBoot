package com.example.AirbnbBookingSpring.controllers;

import com.example.AirbnbBookingSpring.dtos.AirbnbRequestDTO;
import com.example.AirbnbBookingSpring.dtos.AirbnbResponseDTO;
import com.example.AirbnbBookingSpring.services.AirbnbService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airbnb")
@RequiredArgsConstructor
@Slf4j
public class AirbnbController {

    private final AirbnbService airbnbService;

    @PostMapping
    public ResponseEntity<AirbnbResponseDTO> createAirbnb(
            @RequestBody AirbnbRequestDTO createDTO,
            HttpServletRequest request) {
        String ownerEmail = (String) request.getAttribute("email");
        log.info("[AirbnbController] createAirbnb - ownerEmail={}", ownerEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(airbnbService.createAirbnb(createDTO, ownerEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirbnbResponseDTO> getAirbnbById(@PathVariable Long id) {
        return airbnbService.getAirbnbById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AirbnbResponseDTO>> getAllAirbnbs() {
        return ResponseEntity.ok(airbnbService.getAllAirbnb());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirbnbResponseDTO> updateAirbnb(
            @PathVariable Long id,
            @RequestBody AirbnbRequestDTO updateDTO) {
        return ResponseEntity.ok(airbnbService.updateAirbnb(updateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAirbnb(@PathVariable Long id) {
        boolean deleted = airbnbService.deleteAirbnb(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}