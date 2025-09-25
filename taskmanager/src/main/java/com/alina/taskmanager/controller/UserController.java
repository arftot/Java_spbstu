package com.alina.taskmanager.controller;

import com.alina.taskmanager.dto.CreateUserRequest;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@Valid @RequestBody CreateUserRequest request) {
        return userService.register(request);
    }

    @GetMapping("/login")
    public Optional<User> login(@RequestParam String username) {
        return userService.loginByUsername(username);
    }
}


