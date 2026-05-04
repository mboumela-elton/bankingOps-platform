package com.msel.app.service;

import com.msel.app.dto.CreateUserRequest;
import com.msel.app.dto.UserDTO;
import com.msel.app.entity.User;
import com.msel.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .name("John Doe")
                .email("john@example.com")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserById(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(testUserId));
    }

    @Test
    void testGetUserByEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserByEmail("john@example.com");

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void testGetUserByEmailNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserByEmail("nonexistent@example.com"));
    }
}
