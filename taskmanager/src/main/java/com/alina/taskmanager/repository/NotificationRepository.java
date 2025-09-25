package com.alina.taskmanager.repository;

import com.alina.taskmanager.model.Notification;

import java.util.List;

public interface NotificationRepository {
    Notification save(Notification notification);
    List<Notification> findByUserId(String userId);
    List<Notification> findPendingByUserId(String userId);
}


