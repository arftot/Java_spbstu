package com.alina.taskmanager.service.scheduling;

import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.service.NotificationService;
import com.alina.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OverdueTaskCheckerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AsyncNotificationProcessor asyncNotificationProcessor;

    @InjectMocks
    private OverdueTaskChecker overdueTaskChecker;

    private Task testTask1;
    private Task testTask2;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testTask1 = new Task();
        testTask1.setId("task-1");
        testTask1.setUserId("user-1");
        testTask1.setTitle("Overdue Task 1");
        testTask1.setStatus(TaskStatus.PENDING);
        testTask1.setOverdue(false);

        testTask2 = new Task();
        testTask2.setId("task-2");
        testTask2.setUserId("user-2");
        testTask2.setTitle("Overdue Task 2");
        testTask2.setStatus(TaskStatus.PENDING);
        testTask2.setOverdue(false);

        testNotification = new Notification();
        testNotification.setId("notification-1");
        testNotification.setUserId("user-1");
        testNotification.setMessage("New task created: Overdue Task 1");
    }

    @Test
    void checkOverdueTasks_ShouldProcessOverdueTasks() {
        // Given
        List<Task> overdueTasks = Arrays.asList(testTask1, testTask2);
        when(taskService.getOverdueTasks()).thenReturn(overdueTasks);
        when(notificationService.createNotificationForTask(any(Task.class))).thenReturn(testNotification);

        // When
        overdueTaskChecker.checkOverdueTasks();

        // Then
        verify(taskService, times(1)).getOverdueTasks();
        verify(taskService, times(1)).markTaskAsOverdue("task-1");
        verify(taskService, times(1)).markTaskAsOverdue("task-2");
        verify(notificationService, times(1)).createNotificationForTask(testTask1);
        verify(notificationService, times(1)).createNotificationForTask(testTask2);
        verify(asyncNotificationProcessor, times(2)).processNotificationAsync(any(Notification.class));
    }

    @Test
    void checkOverdueTasks_WithNoOverdueTasks_ShouldNotProcess() {
        // Given
        when(taskService.getOverdueTasks()).thenReturn(Arrays.asList());

        // When
        overdueTaskChecker.checkOverdueTasks();

        // Then
        verify(taskService, times(1)).getOverdueTasks();
        verify(taskService, never()).markTaskAsOverdue(any());
        verify(notificationService, never()).createNotificationForTask(any());
        verify(asyncNotificationProcessor, never()).processNotificationAsync(any());
    }

    @Test
    void checkOverdueTasks_WithException_ShouldContinueProcessing() {
        // Given
        Task taskWithError = new Task();
        taskWithError.setId("task-error");
        taskWithError.setUserId("user-error");
        taskWithError.setTitle("Error Task");
        taskWithError.setStatus(TaskStatus.PENDING);

        List<Task> overdueTasks = Arrays.asList(testTask1, taskWithError);
        when(taskService.getOverdueTasks()).thenReturn(overdueTasks);
        when(notificationService.createNotificationForTask(testTask1)).thenReturn(testNotification);
        when(notificationService.createNotificationForTask(taskWithError))
                .thenThrow(new RuntimeException("Notification creation failed"));

        // When
        overdueTaskChecker.checkOverdueTasks();

        // Then
        verify(taskService, times(1)).getOverdueTasks();
        verify(taskService, times(1)).markTaskAsOverdue("task-1");
        verify(taskService, times(1)).markTaskAsOverdue("task-error");
        verify(notificationService, times(1)).createNotificationForTask(testTask1);
        verify(notificationService, times(1)).createNotificationForTask(taskWithError);
        verify(asyncNotificationProcessor, times(1)).processNotificationAsync(any());
    }
}
