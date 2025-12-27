package com.example.AirbnbBookingSpring.dtos;

import com.example.AirbnbBookingSpring.models.Booking;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateBookingRequestDTO {
    @NotNull(message = "Booking ID is required")
    private Long id;

    @NotNull(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotNull(message = "Booking status is required")
    private Booking.BookingStatus bookingStatus;
}
