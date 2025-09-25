package com.alina.taskmanager.service.impl;

import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.repository.NotificationRepository;
import com.alina.taskmanager.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification create(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getByUser(String userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public List<Notification> getPendingByUser(String userId) {
        return notificationRepository.findPendingByUserId(userId);
    }
}


