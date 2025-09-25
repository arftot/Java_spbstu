package com.alina.taskmanager.entity;

import com.alina.taskmanager.model.Notification;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_status", nullable = false)
    private boolean read = false;
    
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    public NotificationEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.read = false;
        this.deleted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public static NotificationEntity fromModel(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(notification.getId());
        entity.setUserId(notification.getUserId());
        entity.setMessage(notification.getMessage());
        entity.setCreatedAt(notification.getCreatedAt() != null ? 
            notification.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        entity.setRead(notification.isRead());
        entity.setDeleted(false); // Not in original model, but needed for soft delete
        return entity;
    }

    public Notification toModel() {
        Notification notification = new Notification();
        notification.setId(this.id);
        notification.setUserId(this.userId);
        notification.setMessage(this.message);
        notification.setCreatedAt(this.createdAt != null ? 
            this.createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        notification.setRead(this.read);
        return notification;
    }
}
