package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.dtos.UserRequestDTO;
import com.example.AirbnbBookingSpring.dtos.UserResponseDTO;
import com.example.AirbnbBookingSpring.mappers.UserMapper;
import com.example.AirbnbBookingSpring.models.User;
import com.example.AirbnbBookingSpring.repositories.writes.UserWriteRepository;
import com.example.AirbnbBookingSpring.services.ImplInterfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserWriteRepository userRepository;

    @Override
    public UserResponseDTO createUser(UserRequestDTO createDTO) {
        User user = UserMapper.toEntity(createDTO);
        User savedUser = userRepository.save(user);
        return UserMapper.toDTO(savedUser);
    }

    @Override
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDTO);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO updateUser(UserRequestDTO updateDTO) {
        User user = UserMapper.toEntity(updateDTO);
        User savedUser = userRepository.save(user);
        return UserMapper.toDTO(savedUser);
    }

    @Override
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

}
