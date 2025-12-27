package com.example.AirbnbBookingSpring.controllers;

import com.example.AirbnbBookingSpring.dtos.AirbnbRequestDTO;
import com.example.AirbnbBookingSpring.dtos.AirbnbResponseDTO;
import com.example.AirbnbBookingSpring.dtos.UserResponseDTO;
import com.example.AirbnbBookingSpring.services.AirbnbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airbnb")
@RequiredArgsConstructor
public class AirbnbController {

    private final AirbnbService airbnbService;

    @PostMapping
    public ResponseEntity<AirbnbResponseDTO> createAirbnb(@RequestBody AirbnbRequestDTO createDTO) {
        AirbnbResponseDTO userCreated = airbnbService.createAirbnb(createDTO);
        return new ResponseEntity<>(userCreated, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirbnbResponseDTO> getAirbnbById(@PathVariable Long id) {
        return airbnbService.getAirbnbById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AirbnbResponseDTO>> getAllAirbnbs() {
        List<AirbnbResponseDTO> airbnbs = airbnbService.getAllAirbnb();
        return ResponseEntity.ok(airbnbs);
    }

    @PutMapping("/update")
    public ResponseEntity<AirbnbResponseDTO> updateAirbnb(
            @RequestBody AirbnbRequestDTO updateDTO) {
        AirbnbResponseDTO updateAirbnb = airbnbService.updateAirbnb(updateDTO);
        return ResponseEntity.ok(updateAirbnb);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAirbnb(@PathVariable Long id) {
        boolean deleted = airbnbService.deleteAirbnb(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
