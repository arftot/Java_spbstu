package com.alina.taskmanager.entity;

import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;
    
    @Column(name = "overdue", nullable = false)
    private boolean overdue = false;

    public TaskEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.deleted = false;
        this.status = TaskStatus.PENDING;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public static TaskEntity fromModel(Task task) {
        TaskEntity entity = new TaskEntity();
        entity.setId(task.getId());
        entity.setUserId(task.getUserId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setCreatedAt(task.getCreatedAt() != null ? 
            task.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        entity.setDueDate(task.getDueDate() != null ? 
            task.getDueDate().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        entity.setDeleted(task.isDeleted());
        entity.setStatus(task.getStatus());
        entity.setOverdue(task.isOverdue());
        return entity;
    }

    public Task toModel() {
        Task task = new Task();
        task.setId(this.id);
        task.setUserId(this.userId);
        task.setTitle(this.title);
        task.setDescription(this.description);
        task.setCreatedAt(this.createdAt != null ? 
            this.createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        task.setDueDate(this.dueDate != null ? 
            this.dueDate.atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        task.setDeleted(this.deleted);
        task.setStatus(this.status);
        task.setOverdue(this.overdue);
        return task;
    }
}
