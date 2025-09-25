package com.alina.taskmanager.repository;

import com.alina.taskmanager.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
}


