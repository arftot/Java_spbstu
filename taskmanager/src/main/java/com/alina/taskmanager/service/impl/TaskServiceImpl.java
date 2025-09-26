package com.alina.taskmanager.service.impl;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.entity.TaskEntity;
import com.alina.taskmanager.entity.UserEntity;
import com.alina.taskmanager.exception.ResourceNotFoundException;
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
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskJpaRepository taskRepository;
    private final UserJpaRepository userRepository;

    public TaskServiceImpl(TaskJpaRepository taskRepository, UserJpaRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
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
        return savedEntity.toModel();
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
}


