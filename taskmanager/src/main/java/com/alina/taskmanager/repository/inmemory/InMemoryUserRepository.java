package com.alina.taskmanager.repository.inmemory;

import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("inmemory")
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> byId = new ConcurrentHashMap<>();
    private final Map<String, User> byUsername = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        byId.put(user.getId(), user);
        if (user.getUsername() != null) {
            byUsername.put(user.getUsername(), user);
        }
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(byUsername.get(username));
    }
}


