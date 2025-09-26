package com.alina.taskmanager.service.scheduling;

import com.alina.taskmanager.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncNotificationProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AsyncNotificationProcessor.class);

    @Async("taskExecutor")
    public CompletableFuture<Void> processNotificationAsync(Notification notification) {
        if (notification == null) {
            logger.warn("Received null notification, skipping processing");
            return CompletableFuture.failedFuture(new IllegalArgumentException("Notification cannot be null"));
        }
        
        logger.info("Processing notification {} asynchronously for user {}", 
                   notification.getId(), notification.getUserId());
        
        try {
            // Simulate heavy processing (e.g., sending email, push notification)
            simulateNotificationSending(notification);
            
            logger.info("Successfully processed notification {} for user {}", 
                       notification.getId(), notification.getUserId());
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Error processing notification {}: {}", 
                        notification.getId(), e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private void simulateNotificationSending(Notification notification) throws InterruptedException {
        // Simulate network delay or heavy processing
        Thread.sleep(1000);
        
        // In a real application, this would:
        // - Send email notification
        // - Send push notification
        // - Update external systems
        // - etc.
        
        logger.debug("Simulated sending notification: {}", notification.getMessage());
    }
}
