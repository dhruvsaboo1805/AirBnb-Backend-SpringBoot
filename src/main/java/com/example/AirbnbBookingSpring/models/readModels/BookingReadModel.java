package com.example.AirbnbBookingSpring.models.readModels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingReadModel {

    private Long id;

    private Long airbnbId;

    private Long userId;

    private Long totalPrice;

    private String bookingStatus;

    private String idempotencyKey;

    private String checkInDate;

    private String checkOutDate;
    
}
