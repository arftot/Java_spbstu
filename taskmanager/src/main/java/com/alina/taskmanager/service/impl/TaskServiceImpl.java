package com.alina.taskmanager.service.impl;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.entity.TaskEntity;
import com.alina.taskmanager.entity.UserEntity;
import com.alina.taskmanager.exception.ResourceNotFoundException;
import com.alina.taskmanager.exception.TaskServiceException;
import com.alina.taskmanager.messaging.TaskPublisher;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.TaskJpaRepository;
import com.alina.taskmanager.repository.UserJpaRepository;
import com.alina.taskmanager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    
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
    public Task createTask(CreateTaskRequest request) throws TaskServiceException {
        try {
            logger.debug("Creating task for user: {}", request.getUserId());
            
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
            
            logger.info("Successfully created task: {} for user: {}", task.getId(), request.getUserId());
            return task;
        } catch (Exception e) {
            logger.error("Error creating task for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new TaskServiceException("Failed to create task", e);
        }
    }

    @Override
    @Cacheable(cacheNames = "tasks", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<Task> getTasksByUser(String userId) throws TaskServiceException {
        try {
            logger.debug("Fetching tasks for user: {}", userId);
            List<Task> tasks = taskRepository.findByUserIdAndDeletedFalse(userId).stream()
                    .map(TaskEntity::toModel)
                    .toList();
            logger.debug("Found {} tasks for user: {}", tasks.size(), userId);
            return tasks;
        } catch (Exception e) {
            logger.error("Error fetching tasks for user {}: {}", userId, e.getMessage(), e);
            throw new TaskServiceException("Failed to fetch tasks for user", e);
        }
    }

    @Override
    @Cacheable(cacheNames = "tasksPending", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<Task> getPendingTasksByUser(String userId) throws TaskServiceException {
        try {
            logger.debug("Fetching pending tasks for user: {}", userId);
            List<Task> tasks = taskRepository.findByUserIdAndDeletedFalse(userId).stream()
                    .filter(task -> task.getStatus() == TaskStatus.PENDING)
                    .map(TaskEntity::toModel)
                    .toList();
            logger.debug("Found {} pending tasks for user: {}", tasks.size(), userId);
            return tasks;
        } catch (Exception e) {
            logger.error("Error fetching pending tasks for user {}: {}", userId, e.getMessage(), e);
            throw new TaskServiceException("Failed to fetch pending tasks for user", e);
        }
    }

    @Override
    @CacheEvict(cacheNames = {"tasks", "tasksPending"}, key = "#taskEntity.userId")
    public void deleteTask(String id) throws TaskServiceException {
        try {
            logger.debug("Deleting task: {}", id);
            TaskEntity taskEntity = taskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
            taskEntity.setDeleted(true);
            taskRepository.save(taskEntity);
            logger.info("Successfully deleted task: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting task {}: {}", id, e.getMessage(), e);
            throw new TaskServiceException("Failed to delete task", e);
        }
    }

    @Override
    public List<Task> getOverdueTasks() throws TaskServiceException {
        try {
            logger.debug("Fetching overdue tasks");
            LocalDateTime now = LocalDateTime.now();
            List<Task> tasks = taskRepository.findOverdueTasks(now).stream()
                    .map(TaskEntity::toModel)
                    .toList();
            logger.debug("Found {} overdue tasks", tasks.size());
            return tasks;
        } catch (Exception e) {
            logger.error("Error fetching overdue tasks: {}", e.getMessage(), e);
            throw new TaskServiceException("Failed to fetch overdue tasks", e);
        }
    }

    @Override
    public void markTaskAsOverdue(String taskId) throws TaskServiceException {
        try {
            logger.debug("Marking task as overdue: {}", taskId);
            TaskEntity taskEntity = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
            taskEntity.setOverdue(true);
            taskRepository.save(taskEntity);
            logger.info("Successfully marked task as overdue: {}", taskId);
        } catch (Exception e) {
            logger.error("Error marking task as overdue {}: {}", taskId, e.getMessage(), e);
            throw new TaskServiceException("Failed to mark task as overdue", e);
        }
    }
}


