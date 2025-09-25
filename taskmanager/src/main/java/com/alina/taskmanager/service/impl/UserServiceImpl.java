package com.alina.taskmanager.service.impl;

import com.alina.taskmanager.dto.CreateUserRequest;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.UserRepository;
import com.alina.taskmanager.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User register(CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        return userRepository.save(user);
    }

    @Override
    public Optional<User> loginByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}


