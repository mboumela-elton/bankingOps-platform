package com.msel.app.service;

import com.msel.app.exception.ResourceNotFoundException;
import com.msel.app.dto.CreateUserRequest;
import com.msel.app.dto.UserDTO;
import com.msel.app.entity.User;
import com.msel.app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        return convertToDTO(savedUser);
    }

    public UserDTO getUserById(UUID userId) {
        log.info("Fetching user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        return convertToDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found");
                });

        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
