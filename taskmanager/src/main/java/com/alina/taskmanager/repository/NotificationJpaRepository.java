package com.alina.taskmanager.repository;

import com.alina.taskmanager.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, String> {
    
    List<NotificationEntity> findByUserIdAndDeletedFalse(String userId);
    
    List<NotificationEntity> findByUserId(String userId);
}
