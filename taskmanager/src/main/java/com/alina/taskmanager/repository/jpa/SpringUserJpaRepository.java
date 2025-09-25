package com.alina.taskmanager.repository.jpa;

import com.alina.taskmanager.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringUserJpaRepository extends JpaRepository<UserEntity, String> {
    
    Optional<UserEntity> findByUsername(String username);
}
