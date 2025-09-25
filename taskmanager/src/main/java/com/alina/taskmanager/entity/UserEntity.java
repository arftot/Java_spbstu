package com.alina.taskmanager.entity;

import com.alina.taskmanager.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private String id;
    
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UserEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static UserEntity fromModel(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setDisplayName(user.getUsername()); // Use username as displayName for now
        entity.setCreatedAt(user.getCreatedAt() != null ? 
            user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        return entity;
    }

    public User toModel() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setCreatedAt(this.createdAt != null ? 
            this.createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        return user;
    }
}
