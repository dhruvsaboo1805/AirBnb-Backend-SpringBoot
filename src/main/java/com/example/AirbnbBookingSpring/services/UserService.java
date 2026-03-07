package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.dtos.UserRequestDTO;
import com.example.AirbnbBookingSpring.dtos.UserResponseDTO;
import com.example.AirbnbBookingSpring.mappers.UserMapper;
import com.example.AirbnbBookingSpring.models.User;
import com.example.AirbnbBookingSpring.repositories.writes.UserWriteRepository;
import com.example.AirbnbBookingSpring.services.ImplInterfaces.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserWriteRepository userRepository;

    @Override
    public UserResponseDTO createUser(UserRequestDTO createDTO) {
        log.info("[createUser] START - email={}, name={}", createDTO.getEmail(), createDTO.getName());

        if (userRepository.findByEmail(createDTO.getEmail()).isPresent()) {
            log.warn("[createUser] Email already in use - email={}", createDTO.getEmail());
            throw new IllegalArgumentException("Email already in use: " + createDTO.getEmail());
        }

        User user = UserMapper.toEntity(createDTO);
        User savedUser = userRepository.save(user);

        log.info("[createUser] END - userId={} created successfully", savedUser.getId());
        return UserMapper.toDTO(savedUser);
    }

    @Override
    public Optional<UserResponseDTO> getUserById(Long id) {
        log.debug("[getUserById] Fetching user - id={}", id);

        Optional<UserResponseDTO> result = userRepository.findById(id).map(UserMapper::toDTO);

        if (result.isEmpty()) {
            log.warn("[getUserById] User not found - id={}", id);
        } else {
            log.debug("[getUserById] User found - id={}", id);
        }

        return result;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.debug("[getAllUsers] Fetching all users");
        List<User> users = userRepository.findAll();
        log.info("[getAllUsers] Found {} users", users.size());
        return users.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO updateUser(UserRequestDTO updateDTO) {
        log.info("[updateUser] START - email={}", updateDTO.getEmail());

        User user = UserMapper.toEntity(updateDTO);
        User savedUser = userRepository.save(user);

        log.info("[updateUser] END - userId={} updated successfully", savedUser.getId());
        return UserMapper.toDTO(savedUser);
    }

    @Override
    public boolean deleteUser(Long id) {
        log.info("[deleteUser] START - id={}", id);

        if (!userRepository.existsById(id)) {
            log.warn("[deleteUser] User not found - id={}", id);
            return false;
        }

        userRepository.deleteById(id);
        log.info("[deleteUser] END - userId={} deleted successfully", id);
        return true;
    }
}