package com.alina.taskmanager.service;

import com.alina.taskmanager.entity.NotificationEntity;
import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.repository.NotificationJpaRepository;
import com.alina.taskmanager.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationJpaRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;
    private NotificationEntity notificationEntity;
    private NotificationEntity readNotificationEntity;
    private NotificationEntity unreadNotificationEntity;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId("notification-id");
        notification.setUserId("user-id");
        notification.setMessage("Test notification message");
        notification.setCreatedAt(Instant.now());
        notification.setRead(false);

        notificationEntity = new NotificationEntity();
        notificationEntity.setId("notification-id");
        notificationEntity.setUserId("user-id");
        notificationEntity.setMessage("Test notification message");
        notificationEntity.setRead(false);
        notificationEntity.setDeleted(false);

        readNotificationEntity = new NotificationEntity();
        readNotificationEntity.setId("read-notification-id");
        readNotificationEntity.setUserId("user-id");
        readNotificationEntity.setMessage("Read notification");
        readNotificationEntity.setRead(true);
        readNotificationEntity.setDeleted(false);

        unreadNotificationEntity = new NotificationEntity();
        unreadNotificationEntity.setId("unread-notification-id");
        unreadNotificationEntity.setUserId("user-id");
        unreadNotificationEntity.setMessage("Unread notification");
        unreadNotificationEntity.setRead(false);
        unreadNotificationEntity.setDeleted(false);
    }

    @Test
    void create_ShouldCreateAndReturnNotification_WhenValidNotification() {
        // Given
        when(notificationRepository.save(any(NotificationEntity.class))).thenReturn(notificationEntity);

        // When
        Notification result = notificationService.create(notification);

        // Then
        assertNotNull(result);
        assertEquals("Test notification message", result.getMessage());
        assertEquals("user-id", result.getUserId());
        assertFalse(result.isRead());
        verify(notificationRepository, times(1)).save(any(NotificationEntity.class));
    }

    @Test
    void getByUser_ShouldReturnNonDeletedNotifications_WhenUserHasNotifications() {
        // Given
        List<NotificationEntity> notificationEntities = Arrays.asList(notificationEntity, readNotificationEntity, unreadNotificationEntity);
        when(notificationRepository.findByUserIdAndDeletedFalse("user-id")).thenReturn(notificationEntities);

        // When
        List<Notification> result = notificationService.getByUser("user-id");

        // Then
        assertEquals(3, result.size());
        verify(notificationRepository, times(1)).findByUserIdAndDeletedFalse("user-id");
    }

    @Test
    void getByUser_ShouldReturnEmptyList_WhenUserHasNoNotifications() {
        // Given
        when(notificationRepository.findByUserIdAndDeletedFalse("user-id")).thenReturn(Arrays.asList());

        // When
        List<Notification> result = notificationService.getByUser("user-id");

        // Then
        assertTrue(result.isEmpty());
        verify(notificationRepository, times(1)).findByUserIdAndDeletedFalse("user-id");
    }

    @Test
    void getPendingByUser_ShouldReturnOnlyUnreadNotifications_WhenUserHasMixedNotifications() {
        // Given
        List<NotificationEntity> notificationEntities = Arrays.asList(readNotificationEntity, unreadNotificationEntity);
        when(notificationRepository.findByUserIdAndDeletedFalse("user-id")).thenReturn(notificationEntities);

        // When
        List<Notification> result = notificationService.getPendingByUser("user-id");

        // Then
        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
        assertEquals("Unread notification", result.get(0).getMessage());
        verify(notificationRepository, times(1)).findByUserIdAndDeletedFalse("user-id");
    }

    @Test
    void getPendingByUser_ShouldReturnEmptyList_WhenUserHasOnlyReadNotifications() {
        // Given
        List<NotificationEntity> notificationEntities = Arrays.asList(readNotificationEntity);
        when(notificationRepository.findByUserIdAndDeletedFalse("user-id")).thenReturn(notificationEntities);

        // When
        List<Notification> result = notificationService.getPendingByUser("user-id");

        // Then
        assertTrue(result.isEmpty());
        verify(notificationRepository, times(1)).findByUserIdAndDeletedFalse("user-id");
    }

    @Test
    void create_ShouldSetCorrectProperties_WhenCreatingNotification() {
        // Given
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity entity = invocation.getArgument(0);
            entity.setId("generated-notification-id");
            return entity;
        });

        // When
        Notification result = notificationService.create(notification);

        // Then
        assertEquals("Test notification message", result.getMessage());
        assertEquals("user-id", result.getUserId());
        assertFalse(result.isRead());
        verify(notificationRepository, times(1)).save(any(NotificationEntity.class));
    }

    @Test
    void getPendingByUser_ShouldFilterCorrectly_WhenUserHasMultipleUnreadNotifications() {
        // Given
        NotificationEntity anotherUnreadEntity = new NotificationEntity();
        anotherUnreadEntity.setId("another-unread-id");
        anotherUnreadEntity.setUserId("user-id");
        anotherUnreadEntity.setMessage("Another unread notification");
        anotherUnreadEntity.setRead(false);
        anotherUnreadEntity.setDeleted(false);

        List<NotificationEntity> notificationEntities = Arrays.asList(
            readNotificationEntity, 
            unreadNotificationEntity, 
            anotherUnreadEntity
        );
        when(notificationRepository.findByUserIdAndDeletedFalse("user-id")).thenReturn(notificationEntities);

        // When
        List<Notification> result = notificationService.getPendingByUser("user-id");

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(Notification::isRead));
        verify(notificationRepository, times(1)).findByUserIdAndDeletedFalse("user-id");
    }
}