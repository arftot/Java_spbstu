package com.alina.taskmanager.repository.adapter;

import com.alina.taskmanager.entity.UserEntity;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.UserRepository;
import com.alina.taskmanager.repository.jpa.SpringUserJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("db")
public class UserRepositoryAdapter implements UserRepository {
    
    private final SpringUserJpaRepository jpaRepository;
    
    public UserRepositoryAdapter(SpringUserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.fromModel(user);
        UserEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toModel();
    }
    
    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findById(id)
                .map(UserEntity::toModel);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(UserEntity::toModel);
    }
}
