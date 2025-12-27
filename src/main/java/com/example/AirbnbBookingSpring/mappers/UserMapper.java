package com.example.AirbnbBookingSpring.mappers;

import com.example.AirbnbBookingSpring.dtos.UserRequestDTO;
import com.example.AirbnbBookingSpring.dtos.UserResponseDTO;
import com.example.AirbnbBookingSpring.models.User;

public class UserMapper {
    public static User toEntity(UserRequestDTO userRequestDTO) {
        return User.builder()
                .name(userRequestDTO.getName())
                .email(userRequestDTO.getEmail())
                .password(userRequestDTO.getPassword())
                .build();
    }

    public static UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .airbnbs(user.getAirbnbList())
                .bookings(user.getBookingList())
                .build();
    }
}
