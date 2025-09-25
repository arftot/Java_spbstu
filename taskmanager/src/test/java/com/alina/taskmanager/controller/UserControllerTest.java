package com.alina.taskmanager.controller;

import com.alina.taskmanager.dto.CreateUserRequest;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@SuppressWarnings("deprecation")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private CreateUserRequest createUserRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setCreatedAt(Instant.now());

        createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testuser");
    }

    @Test
    void registerUser_ShouldReturnCreatedUser_WhenValidRequest() throws Exception {
        when(userService.register(any(CreateUserRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("user-123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest();
        invalidRequest.setUsername(""); // Empty username should fail validation

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void loginUser_ShouldReturnUser_WhenUserExists() throws Exception {
        when(userService.loginByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/login")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-123"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void loginUser_ShouldReturnEmpty_WhenUserDoesNotExist() throws Exception {
        when(userService.loginByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/login")
                        .param("username", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(content().string("null"));
    }
}
