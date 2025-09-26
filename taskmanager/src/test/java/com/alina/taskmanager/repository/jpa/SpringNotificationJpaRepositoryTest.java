package com.alina.taskmanager.repository.jpa;

import com.alina.taskmanager.entity.NotificationEntity;
import com.alina.taskmanager.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("db")
class SpringNotificationJpaRepositoryTest {

    @Autowired
    private SpringNotificationJpaRepository repository;
    
    @Autowired
    private SpringUserJpaRepository userRepository;

    private NotificationEntity testNotification;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // Create a test user first
        testUser = new UserEntity();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setDisplayName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(testUser);
        
        testNotification = new NotificationEntity();
        testNotification.setUserId("user-123");
        testNotification.setMessage("Test notification");
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification.setDeleted(false);
    }

    @Test
    void save_ShouldReturnSavedNotification() {
        NotificationEntity saved = repository.save(testNotification);

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getMessage()).isEqualTo("Test notification");
        assertThat(saved.getUserId()).isEqualTo("user-123");
    }

    @Test
    void findByUserIdAndNotDeleted_ShouldReturnNotifications_WhenUserHasNotifications() {
        repository.save(testNotification);

        List<NotificationEntity> notifications = repository.findByUserIdAndNotDeleted("user-123");

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).isEqualTo("Test notification");
    }

    @Test
    void findPendingByUserIdAndNotDeleted_ShouldReturnUnreadNotifications() {
        repository.save(testNotification);

        List<NotificationEntity> notifications = repository.findPendingByUserIdAndNotDeleted("user-123");

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).isRead()).isFalse();
    }
}
