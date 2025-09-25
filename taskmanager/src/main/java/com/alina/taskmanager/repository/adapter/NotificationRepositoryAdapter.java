package com.alina.taskmanager.repository.adapter;

import com.alina.taskmanager.entity.NotificationEntity;
import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.repository.NotificationRepository;
import com.alina.taskmanager.repository.jpa.SpringNotificationJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Profile("db")
public class NotificationRepositoryAdapter implements NotificationRepository {
    
    private final SpringNotificationJpaRepository jpaRepository;
    
    public NotificationRepositoryAdapter(SpringNotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = NotificationEntity.fromModel(notification);
        NotificationEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toModel();
    }
    
    @Override
    public List<Notification> findByUserId(String userId) {
        return jpaRepository.findByUserIdAndNotDeleted(userId)
                .stream()
                .map(NotificationEntity::toModel)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Notification> findPendingByUserId(String userId) {
        return jpaRepository.findPendingByUserIdAndNotDeleted(userId)
                .stream()
                .map(NotificationEntity::toModel)
                .collect(Collectors.toList());
    }
}
