package com.example.AirbnbBookingSpring.repositories.writes;

import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.models.readModels.BookingReadModel;
import com.example.AirbnbBookingSpring.repositories.reads.RedisReadRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisWriteRepository {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public void writeBookingReadModel(Booking booking) throws JsonProcessingException {
        BookingReadModel bookingReadModel = BookingReadModel.builder()
                .id(booking.getId())
                .airbnbId(booking.getAirbnbId())
                .userId(booking.getUserId())
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus().name())
                .idempotencyKey(booking.getIdempotencyKey())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .build();

        saveBookingReadModel(bookingReadModel);
    }

    private void saveBookingReadModel(BookingReadModel bookingReadModel) throws JsonProcessingException {
        String key = RedisReadRepository.BOOKING_KEY_PREFIX + bookingReadModel.getId();
        String value = objectMapper.writeValueAsString(bookingReadModel);
        redisTemplate.opsForValue().set(key, value);
    }
}
