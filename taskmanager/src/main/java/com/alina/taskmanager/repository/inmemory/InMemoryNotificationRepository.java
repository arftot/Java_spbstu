package com.alina.taskmanager.repository.inmemory;

import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.repository.NotificationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Profile("inmemory")
public class InMemoryNotificationRepository implements NotificationRepository {
    private final Map<String, Notification> storage = new ConcurrentHashMap<>();

    @Override
    public Notification save(Notification notification) {
        storage.put(notification.getId(), notification);
        return notification;
    }

    @Override
    public List<Notification> findByUserId(String userId) {
        return storage.values().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findPendingByUserId(String userId) {
        return storage.values().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
    }
}


