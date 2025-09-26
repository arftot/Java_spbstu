package com.alina.taskmanager.service;

import com.alina.taskmanager.dto.CreateUserRequest;
import com.alina.taskmanager.entity.UserEntity;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.UserJpaRepository;
import com.alina.taskmanager.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private UserEntity userEntity;
    private User expectedUser;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testuser");

        userEntity = new UserEntity();
        userEntity.setId("test-id");
        userEntity.setUsername("testuser");
        userEntity.setDisplayName("testuser");
        userEntity.setCreatedAt(LocalDateTime.now());

        expectedUser = new User();
        expectedUser.setId("test-id");
        expectedUser.setUsername("testuser");
        expectedUser.setCreatedAt(Instant.now());
    }

    @Test
    void register_ShouldCreateAndReturnUser_WhenValidRequest() {
        // Given
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // When
        User result = userService.register(createUserRequest);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void loginByUsername_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        // When
        Optional<User> result = userService.loginByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void loginByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.loginByUsername("nonexistent");

        // Then
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void register_ShouldSetCorrectUsername_WhenCreatingUser() {
        // Given
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId("generated-id");
            return entity;
        });

        // When
        User result = userService.register(createUserRequest);

        // Then
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
}