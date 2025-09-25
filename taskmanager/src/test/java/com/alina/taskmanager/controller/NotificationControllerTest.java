package com.alina.taskmanager.controller;

import com.alina.taskmanager.model.Notification;
import com.alina.taskmanager.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@SuppressWarnings("deprecation")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

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
    void getNotifications_ShouldReturnNotifications_WhenUserExists() throws Exception {
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationService.getByUser("user-123")).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications")
                        .param("userId", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("notification-123"))
                .andExpect(jsonPath("$[0].userId").value("user-123"))
                .andExpect(jsonPath("$[0].message").value("Test notification message"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void getPendingNotifications_ShouldReturnPendingNotifications_WhenUserExists() throws Exception {
        List<Notification> pendingNotifications = Arrays.asList(testNotification);
        when(notificationService.getPendingByUser("user-123")).thenReturn(pendingNotifications);

        mockMvc.perform(get("/api/notifications/pending")
                        .param("userId", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("notification-123"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void getNotifications_ShouldReturnEmptyList_WhenUserHasNoNotifications() throws Exception {
        when(notificationService.getByUser("user-456")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/notifications")
                        .param("userId", "user-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getPendingNotifications_ShouldReturnEmptyList_WhenUserHasNoPendingNotifications() throws Exception {
        when(notificationService.getPendingByUser("user-456")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/notifications/pending")
                        .param("userId", "user-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
