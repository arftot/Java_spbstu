package com.alina.taskmanager.service;

import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private com.alina.taskmanager.service.impl.NotificationServiceImpl notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId("notification-123");
        testNotification.setUserId("user-123");
        testNotification.setMessage("Test notification message");
        testNotification.setRead(false);
        testNotification.setCreatedAt(Instant.now());
    }

    @Test
    void create_ShouldReturnCreatedNotification_WhenValidNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        Notification result = notificationService.create(testNotification);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("notification-123");
        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getMessage()).isEqualTo("Test notification message");
        assertThat(result.isRead()).isFalse();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void create_ShouldSetIdAndTimestamp_WhenNewNotification() {
        Notification newNotification = new Notification();
        newNotification.setUserId("user-123");
        newNotification.setMessage("New notification");

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId("notification-456");
            notification.setCreatedAt(Instant.now());
            return notification;
        });

        Notification result = notificationService.create(newNotification);

        assertThat(result.getId()).isNotBlank();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.isRead()).isFalse();
    }

    @Test
    void getByUser_ShouldReturnNotifications_WhenUserExists() {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserId("user-123")).thenReturn(notifications);

        List<Notification> result = notificationService.getByUser("user-123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("Test notification message");
        verify(notificationRepository).findByUserId("user-123");
    }

    @Test
    void getByUser_ShouldReturnEmptyList_WhenUserHasNoNotifications() {
        when(notificationRepository.findByUserId("user-456")).thenReturn(Arrays.asList());

        List<Notification> result = notificationService.getByUser("user-456");

        assertThat(result).isEmpty();
        verify(notificationRepository).findByUserId("user-456");
    }

    @Test
    void getPendingByUser_ShouldReturnPendingNotifications_WhenUserExists() {
        List<Notification> pendingNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findPendingByUserId("user-123")).thenReturn(pendingNotifications);

        List<Notification> result = notificationService.getPendingByUser("user-123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isRead()).isFalse();
        verify(notificationRepository).findPendingByUserId("user-123");
    }

    @Test
    void getPendingByUser_ShouldReturnEmptyList_WhenUserHasNoPendingNotifications() {
        when(notificationRepository.findPendingByUserId("user-456")).thenReturn(Arrays.asList());

        List<Notification> result = notificationService.getPendingByUser("user-456");

        assertThat(result).isEmpty();
        verify(notificationRepository).findPendingByUserId("user-456");
    }

    @Test
    void getPendingByUser_ShouldOnlyReturnUnreadNotifications() {
        Notification readNotification = new Notification();
        readNotification.setId("notification-read");
        readNotification.setUserId("user-123");
        readNotification.setMessage("Read notification");
        readNotification.setRead(true);

        List<Notification> allNotifications = Arrays.asList(testNotification, readNotification);
        when(notificationRepository.findByUserId("user-123")).thenReturn(allNotifications);
        when(notificationRepository.findPendingByUserId("user-123")).thenReturn(Arrays.asList(testNotification));

        List<Notification> allResult = notificationService.getByUser("user-123");
        List<Notification> pendingResult = notificationService.getPendingByUser("user-123");

        assertThat(allResult).hasSize(2);
        assertThat(pendingResult).hasSize(1);
        assertThat(pendingResult.get(0).isRead()).isFalse();
    }
}
