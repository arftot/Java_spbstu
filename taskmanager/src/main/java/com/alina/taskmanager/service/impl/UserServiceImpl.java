package com.alina.taskmanager.service.impl;

import com.alina.taskmanager.dto.CreateUserRequest;
import com.alina.taskmanager.entity.UserEntity;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.UserJpaRepository;
import com.alina.taskmanager.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserJpaRepository userRepository;

    public UserServiceImpl(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User register(CreateUserRequest request) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(request.getUsername());
        UserEntity savedEntity = userRepository.save(userEntity);
        return savedEntity.toModel();
    }

    @Override
    public Optional<User> loginByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserEntity::toModel);
    }
}


