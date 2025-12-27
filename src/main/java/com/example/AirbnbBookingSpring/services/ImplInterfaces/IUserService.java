package com.example.AirbnbBookingSpring.services.ImplInterfaces;

import com.example.AirbnbBookingSpring.dtos.UserRequestDTO;
import com.example.AirbnbBookingSpring.dtos.UserResponseDTO;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO);
    public Optional<UserResponseDTO> getUserById(Long id);
    public List<UserResponseDTO> getAllUsers();
    public UserResponseDTO updateUser(UserRequestDTO userRequestDTO);
    public boolean deleteUser(Long id);
}
