package com.alina.taskmanager.service.scheduling;

import com.alina.taskmanager.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AsyncNotificationProcessorTest {

    @InjectMocks
    private AsyncNotificationProcessor asyncNotificationProcessor;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId("notification-1");
        testNotification.setUserId("user-1");
        testNotification.setMessage("Test notification message");
    }

    @Test
    void processNotificationAsync_ShouldCompleteSuccessfully() throws Exception {
        // When
        CompletableFuture<Void> future = asyncNotificationProcessor.processNotificationAsync(testNotification);

        // Then
        assertNotNull(future);
        assertDoesNotThrow(() -> future.get(5, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void processNotificationAsync_WithNullNotification_ShouldHandleGracefully() throws Exception {
        // When
        CompletableFuture<Void> future = asyncNotificationProcessor.processNotificationAsync(null);

        // Then
        assertNotNull(future);
        // Should complete with exception
        assertThrows(Exception.class, () -> future.get(5, TimeUnit.SECONDS));
    }

    @Test
    void processNotificationAsync_ShouldReturnCompletableFuture() {
        // When
        CompletableFuture<Void> future = asyncNotificationProcessor.processNotificationAsync(testNotification);

        // Then
        assertNotNull(future);
        assertTrue(future instanceof CompletableFuture);
    }
}
