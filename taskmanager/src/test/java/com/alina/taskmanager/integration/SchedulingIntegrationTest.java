package com.alina.taskmanager.integration;

import com.alina.taskmanager.entity.TaskEntity;
import com.alina.taskmanager.entity.UserEntity;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.repository.NotificationJpaRepository;
import com.alina.taskmanager.repository.TaskJpaRepository;
import com.alina.taskmanager.repository.UserJpaRepository;
import com.alina.taskmanager.service.scheduling.OverdueTaskChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "scheduler.fixedDelay.ms=1000",
    "scheduler.initialDelay.ms=100",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "spring.redis.host=localhost",
    "spring.redis.port=6379",
    "spring.cache.type=simple"
})
class SchedulingIntegrationTest {

    @Autowired
    private OverdueTaskChecker overdueTaskChecker;

    @Autowired
    private TaskJpaRepository taskRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private NotificationJpaRepository notificationRepository;

    private UserEntity testUser;
    private TaskEntity overdueTask;

    @BeforeEach
    void setUp() {
        // Clean up
        notificationRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new UserEntity();
        testUser.setId("test-user-1");
        testUser.setUsername("testuser");
        testUser.setDisplayName("Test User");
        testUser = userRepository.save(testUser);

        // Create overdue task
        overdueTask = new TaskEntity();
        overdueTask.setId("overdue-task-1");
        overdueTask.setUserId(testUser.getId());
        overdueTask.setTitle("Overdue Task");
        overdueTask.setDescription("This task is overdue");
        overdueTask.setStatus(TaskStatus.PENDING);
        overdueTask.setDueDate(LocalDateTime.now().minusHours(1)); // 1 hour ago
        overdueTask.setOverdue(false);
        overdueTask.setDeleted(false);
        overdueTask = taskRepository.save(overdueTask);
    }

    @Test
    void checkOverdueTasks_ShouldProcessOverdueTaskAndCreateNotification() throws InterruptedException {
        // Given - task is already overdue (created 1 hour ago)
        assertEquals(0, notificationRepository.count());

        // When - wait for scheduler to run (initial delay + processing time)
        Thread.sleep(2000);

        // Then - check that task was marked as overdue
        TaskEntity updatedTask = taskRepository.findById(overdueTask.getId()).orElseThrow();
        assertTrue(updatedTask.isOverdue());

        // And notification was created
        List<com.alina.taskmanager.entity.NotificationEntity> notifications = 
            notificationRepository.findByUserIdAndDeletedFalse(testUser.getId());
        assertEquals(1, notifications.size());
        
        com.alina.taskmanager.entity.NotificationEntity notification = notifications.get(0);
        assertEquals(testUser.getId(), notification.getUserId());
        assertTrue(notification.getMessage().contains("Overdue Task"));
        assertEquals(overdueTask.getId(), notification.getTaskId());
    }

    @Test
    void checkOverdueTasks_WithNoOverdueTasks_ShouldNotCreateNotifications() throws InterruptedException {
        // Given - delete the overdue task
        taskRepository.delete(overdueTask);
        assertEquals(0, notificationRepository.count());

        // When - wait for scheduler to run
        Thread.sleep(2000);

        // Then - no notifications should be created
        assertEquals(0, notificationRepository.count());
    }

    @Test
    void checkOverdueTasks_WithFutureDueDate_ShouldNotProcess() throws InterruptedException {
        // Given - update task to have future due date
        overdueTask.setDueDate(LocalDateTime.now().plusHours(1));
        taskRepository.save(overdueTask);
        assertEquals(0, notificationRepository.count());

        // When - wait for scheduler to run
        Thread.sleep(2000);

        // Then - task should not be marked as overdue
        TaskEntity updatedTask = taskRepository.findById(overdueTask.getId()).orElseThrow();
        assertFalse(updatedTask.isOverdue());
        assertEquals(0, notificationRepository.count());
    }
}
