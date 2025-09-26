package com.alina.taskmanager.service;

import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.model.Task;

import java.util.List;

public interface NotificationService {
    Notification create(Notification notification);
    List<Notification> getByUser(String userId);
    List<Notification> getPendingByUser(String userId);
    Notification createNotificationForTask(Task task);
}


