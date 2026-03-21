package com.example.AirbnbBookingSpring.repositories.writes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.AirbnbBookingSpring.models.Booking;

@Repository
public interface BookingWriteRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByAirbnbId(Long airbnbId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.airbnb WHERE b.idempotencyKey = :key")
    Optional<Booking> findByIdempotencyKey(@Param("key") String idempotencyKey);
}