package com.alina.taskmanager.messaging;

import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.service.NotificationService;
import com.alina.taskmanager.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {
    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitConfig.TASK_QUEUE)
    public void receiveTask(Task task) {
        notificationService.createNotificationForTask(task);
    }
}
