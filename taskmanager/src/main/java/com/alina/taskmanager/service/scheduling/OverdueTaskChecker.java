package com.alina.taskmanager.service.scheduling;

import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.service.NotificationService;
import com.alina.taskmanager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OverdueTaskChecker {
    private static final Logger logger = LoggerFactory.getLogger(OverdueTaskChecker.class);
    
    private final TaskService taskService;
    private final NotificationService notificationService;
    private final AsyncNotificationProcessor asyncNotificationProcessor;

    public OverdueTaskChecker(TaskService taskService, 
                             NotificationService notificationService,
                             AsyncNotificationProcessor asyncNotificationProcessor) {
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.asyncNotificationProcessor = asyncNotificationProcessor;
    }

    @Scheduled(fixedDelayString = "${scheduler.fixedDelay.ms:60000}", 
               initialDelayString = "${scheduler.initialDelay.ms:10000}")
    public void checkOverdueTasks() {
        logger.info("Starting overdue tasks check...");
        
        try {
            List<Task> overdueTasks = taskService.getOverdueTasks();
            logger.info("Found {} overdue tasks", overdueTasks.size());
            
            for (Task task : overdueTasks) {
                try {
                    processOverdueTask(task);
                } catch (Exception e) {
                    logger.error("Error processing overdue task {}: {}", task.getId(), e.getMessage(), e);
                }
            }
            
            logger.info("Completed overdue tasks check. Processed {} tasks", overdueTasks.size());
        } catch (Exception e) {
            logger.error("Error during overdue tasks check: {}", e.getMessage(), e);
        }
    }

    private void processOverdueTask(Task task) {
        logger.info("Processing overdue task: {} - {}", task.getId(), task.getTitle());
        
        // Mark task as overdue
        taskService.markTaskAsOverdue(task.getId());
        
        // Create notification
        Notification notification = notificationService.createNotificationForTask(task);
        logger.info("Created notification {} for overdue task {}", notification.getId(), task.getId());
        
        // Process notification asynchronously
        asyncNotificationProcessor.processNotificationAsync(notification);
    }
}
