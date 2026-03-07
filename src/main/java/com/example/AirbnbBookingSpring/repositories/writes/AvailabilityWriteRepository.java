package com.example.AirbnbBookingSpring.repositories.writes;

import java.time.LocalDate;
import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.AirbnbBookingSpring.models.Availability;

@Repository
public interface AvailabilityWriteRepository extends JpaRepository<Availability, Long> {
    
    List<Availability> findByBookingId(Long bookingId);

    List<Availability> findByAirbnbId(Long airbnbId);

    // SELECT * FROM availability WHERE airbnb_id = airbnbdId AND date BETWEEN startDate AND endDate;
    List<Availability> findByAirbnbIdAndDateBetween(Long airbnbId, LocalDate startDate, LocalDate endDate);


    // SELECT COUNT(*) FROM availability WHERE airbnb_id = airbnbdId AND date BETWEEN startDate AND endDate AND booking_id IS NOT NULL;
    Long countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull(Long airbnbId, LocalDate startDate, LocalDate endDate);

    // UPDATE availability SET booking_id = bookingId where airbnb_id = airbnbId and date BETWEEN startDate AND endDate;
    @Modifying(clearAutomatically = true, flushAutomatically = true)  // ✅
    @Transactional
    @Query("UPDATE Availability a SET a.bookingId = :bookingId WHERE a.airbnbId = :airbnbId AND a.date BETWEEN :startDate AND :endDate")
    int updateBookingIdByAirbnbIdAndDateBetween(
            @Param("bookingId") Long bookingId,
            @Param("airbnbId") Long airbnbId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(a) FROM Availability a WHERE a.airbnb.id = :airbnbId " +
            "AND a.date BETWEEN :checkInDate AND :checkOutDate " +
            "AND a.bookingId IS NOT NULL " +
            "AND a.bookingId != :bookingId")
    Long countBookedSlotsExcludingBooking(
            @Param("airbnbId") Long airbnbId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("bookingId") Long bookingId
    );

    // for pesimistic locking

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("""
//        SELECT a FROM Availability a
//        WHERE a.airbnbId = :airbnbId
//        AND a.date BETWEEN :checkIn AND :checkOut
//    """)
//    List<Availability> findAndLockDates(
//            Long airbnbId,
//            LocalDate checkIn,
//            LocalDate checkOut
//    );

    // for optimistic locking its same thing only but declare version number in available entities
}
