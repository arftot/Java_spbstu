package com.alina.taskmanager.controller;

import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> getByUser(@RequestParam String userId) {
        return notificationService.getByUser(userId);
    }

    @GetMapping("/pending")
    public List<Notification> getPendingByUser(@RequestParam String userId) {
        return notificationService.getPendingByUser(userId);
    }
}


