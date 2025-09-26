package com.alina.taskmanager.service.impl;

import com.alina.taskmanager.entity.NotificationEntity;
import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.repository.NotificationJpaRepository;
import com.alina.taskmanager.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationJpaRepository notificationRepository;

    public NotificationServiceImpl(NotificationJpaRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification create(Notification notification) {
        NotificationEntity entity = NotificationEntity.fromModel(notification);
        NotificationEntity savedEntity = notificationRepository.save(entity);
        return savedEntity.toModel();
    }

    @Override
    public List<Notification> getByUser(String userId) {
        return notificationRepository.findByUserIdAndDeletedFalse(userId).stream()
                .map(NotificationEntity::toModel)
                .toList();
    }

    @Override
    public List<Notification> getPendingByUser(String userId) {
        return notificationRepository.findByUserIdAndDeletedFalse(userId).stream()
                .filter(notification -> !notification.isRead())
                .map(NotificationEntity::toModel)
                .toList();
    }

    @Override
    public Notification createNotificationForTask(Task task) {
        Notification notification = new Notification();
        notification.setUserId(task.getUserId());
        notification.setMessage("New task created: " + task.getTitle());
        notification.setTaskId(task.getId());
        return create(notification);
    }
}


