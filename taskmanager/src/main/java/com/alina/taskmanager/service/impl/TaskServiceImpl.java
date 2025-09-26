package com.alina.taskmanager.service.impl;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.entity.TaskEntity;
import com.alina.taskmanager.entity.UserEntity;
import com.alina.taskmanager.exception.ResourceNotFoundException;
import com.alina.taskmanager.messaging.TaskPublisher;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.TaskJpaRepository;
import com.alina.taskmanager.repository.UserJpaRepository;
import com.alina.taskmanager.service.TaskService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskJpaRepository taskRepository;
    private final UserJpaRepository userRepository;
    private final TaskPublisher taskPublisher;

    public TaskServiceImpl(TaskJpaRepository taskRepository, UserJpaRepository userRepository, TaskPublisher taskPublisher) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskPublisher = taskPublisher;
    }

    @Override
    @CacheEvict(cacheNames = {"tasks", "tasksPending"}, key = "#request.userId")
    public Task createTask(CreateTaskRequest request) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUserId(user.getId());
        taskEntity.setTitle(request.getTitle());
        taskEntity.setDescription(request.getDescription());
        taskEntity.setDueDate(request.getDueDate() != null ? 
            request.getDueDate().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        taskEntity.setStatus(TaskStatus.PENDING);
        taskEntity.setCreatedAt(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        TaskEntity savedEntity = taskRepository.save(taskEntity);
        Task task = savedEntity.toModel();
        
        // Publish task creation event
        taskPublisher.publishTask(task);
        
        return task;
    }

    @Override
    @Cacheable(cacheNames = "tasks", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<Task> getTasksByUser(String userId) {
        return taskRepository.findByUserIdAndDeletedFalse(userId).stream()
                .map(TaskEntity::toModel)
                .toList();
    }

    @Override
    @Cacheable(cacheNames = "tasksPending", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<Task> getPendingTasksByUser(String userId) {
        return taskRepository.findByUserIdAndDeletedFalse(userId).stream()
                .filter(task -> task.getStatus() == TaskStatus.PENDING)
                .map(TaskEntity::toModel)
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = {"tasks", "tasksPending"}, key = "#taskEntity.userId")
    public void deleteTask(String id) {
        TaskEntity taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        taskEntity.setDeleted(true);
        taskRepository.save(taskEntity);
    }

    @Override
    public List<Task> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.findOverdueTasks(now).stream()
                .map(TaskEntity::toModel)
                .toList();
    }

    @Override
    public void markTaskAsOverdue(String taskId) {
        TaskEntity taskEntity = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        taskEntity.setOverdue(true);
        taskRepository.save(taskEntity);
    }
}


