package com.alina.taskmanager.service;

import com.alina.taskmanager.dto.CreateUserRequest;
import com.alina.taskmanager.model.User;

import java.util.Optional;

public interface UserService {
    User register(CreateUserRequest request);
    Optional<User> loginByUsername(String username);
}


