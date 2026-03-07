package com.example.AirbnbBookingSpring.repositories.writes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.AirbnbBookingSpring.models.Booking;

import jakarta.persistence.LockModeType;

@Repository
public interface BookingWriteRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByAirbnbId(Long airbnbId);

    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    // @Query("SELECT b FROM Booking b WHERE b.id = :id")
    // Optional<Booking> findByIdWithLock( @Param("id") Long id);

    // In your BookingWriteRepository (or wherever idempotencyService queries from)
    @Query("SELECT b FROM Booking b JOIN FETCH b.airbnb JOIN FETCH b.user WHERE b.idempotencyKey = :key")
    Optional<Booking> findByIdempotencyKey(@Param("key") String idempotencyKey);
}
