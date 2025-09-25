package com.alina.taskmanager.service;

import com.alina.taskmanager.dto.CreateUserRequest;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private com.alina.taskmanager.service.impl.UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testuser");

        testUser = new User();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setCreatedAt(Instant.now());
    }

    @Test
    void registerUser_ShouldReturnCreatedUser_WhenValidRequest() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.register(createUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getId()).isNotBlank();
        assertThat(result.getCreatedAt()).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldSetUsername_WhenValidRequest() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("user-123");
            return user;
        });

        User result = userService.register(createUserRequest);

        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginByUsername_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.loginByUsername("testuser");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getId()).isEqualTo("user-123");
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loginByUsername_ShouldReturnEmpty_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.loginByUsername("nonexistent");

        assertThat(result).isEmpty();
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void loginByUsername_ShouldReturnEmpty_WhenUsernameIsNull() {
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        Optional<User> result = userService.loginByUsername(null);

        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(null);
    }
}
