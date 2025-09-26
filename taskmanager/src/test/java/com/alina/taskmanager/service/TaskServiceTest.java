package com.alina.taskmanager.service;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.entity.TaskEntity;
import com.alina.taskmanager.entity.UserEntity;
import com.alina.taskmanager.exception.ResourceNotFoundException;
import com.alina.taskmanager.exception.TaskServiceException;
import com.alina.taskmanager.messaging.TaskPublisher;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.repository.TaskJpaRepository;
import com.alina.taskmanager.repository.UserJpaRepository;
import com.alina.taskmanager.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskJpaRepository taskRepository;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private TaskPublisher taskPublisher;

    @InjectMocks
    private TaskServiceImpl taskService;

    private CreateTaskRequest createTaskRequest;
    private UserEntity userEntity;
    private TaskEntity taskEntity;
    private TaskEntity pendingTaskEntity;
    private TaskEntity completedTaskEntity;

    @BeforeEach
    void setUp() {
        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setUserId("user-id");
        createTaskRequest.setTitle("Test Task");
        createTaskRequest.setDescription("Test Description");
        createTaskRequest.setDueDate(Instant.now().plusSeconds(86400)); // 1 day from now

        userEntity = new UserEntity();
        userEntity.setId("user-id");
        userEntity.setUsername("testuser");

        taskEntity = new TaskEntity();
        taskEntity.setId("task-id");
        taskEntity.setUserId("user-id");
        taskEntity.setTitle("Test Task");
        taskEntity.setDescription("Test Description");
        taskEntity.setStatus(TaskStatus.PENDING);
        taskEntity.setDeleted(false);

        pendingTaskEntity = new TaskEntity();
        pendingTaskEntity.setId("pending-task-id");
        pendingTaskEntity.setUserId("user-id");
        pendingTaskEntity.setTitle("Pending Task");
        pendingTaskEntity.setStatus(TaskStatus.PENDING);
        pendingTaskEntity.setDeleted(false);

        completedTaskEntity = new TaskEntity();
        completedTaskEntity.setId("completed-task-id");
        completedTaskEntity.setUserId("user-id");
        completedTaskEntity.setTitle("Completed Task");
        completedTaskEntity.setStatus(TaskStatus.COMPLETED);
        completedTaskEntity.setDeleted(false);
    }

    @Test
    void createTask_ShouldCreateAndReturnTask_WhenValidRequest() {
        // Given
        when(userRepository.findById("user-id")).thenReturn(Optional.of(userEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(taskEntity);
        doNothing().when(taskPublisher).publishTask(any(Task.class));

        // When
        Task result = taskService.createTask(createTaskRequest);

        // Then
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(TaskStatus.PENDING, result.getStatus());
        verify(userRepository, times(1)).findById("user-id");
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test
    void createTask_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById("user-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskServiceException.class, () -> {
            taskService.createTask(createTaskRequest);
        });
        verify(userRepository, times(1)).findById("user-id");
        verify(taskRepository, never()).save(any(TaskEntity.class));
    }

    @Test
    void getTasksByUser_ShouldReturnNonDeletedTasks_WhenUserHasTasks() {
        // Given
        List<TaskEntity> taskEntities = Arrays.asList(taskEntity, pendingTaskEntity, completedTaskEntity);
        when(taskRepository.findByUserIdAndDeletedFalse("user-id")).thenReturn(taskEntities);

        // When
        List<Task> result = taskService.getTasksByUser("user-id");

        // Then
        assertEquals(3, result.size());
        verify(taskRepository, times(1)).findByUserIdAndDeletedFalse("user-id");
    }

    @Test
    void getTasksByUser_ShouldReturnEmptyList_WhenUserHasNoTasks() {
        // Given
        when(taskRepository.findByUserIdAndDeletedFalse("user-id")).thenReturn(Arrays.asList());

        // When
        List<Task> result = taskService.getTasksByUser("user-id");

        // Then
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findByUserIdAndDeletedFalse("user-id");
    }

    @Test
    void getPendingTasksByUser_ShouldReturnOnlyPendingTasks_WhenUserHasMixedTasks() {
        // Given
        List<TaskEntity> taskEntities = Arrays.asList(pendingTaskEntity, completedTaskEntity);
        when(taskRepository.findByUserIdAndDeletedFalse("user-id")).thenReturn(taskEntities);

        // When
        List<Task> result = taskService.getPendingTasksByUser("user-id");

        // Then
        assertEquals(1, result.size());
        assertEquals(TaskStatus.PENDING, result.get(0).getStatus());
        verify(taskRepository, times(1)).findByUserIdAndDeletedFalse("user-id");
    }

    @Test
    void deleteTask_ShouldMarkTaskAsDeleted_WhenTaskExists() {
        // Given
        when(taskRepository.findById("task-id")).thenReturn(Optional.of(taskEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(taskEntity);

        // When
        taskService.deleteTask("task-id");

        // Then
        assertTrue(taskEntity.isDeleted());
        verify(taskRepository, times(1)).findById("task-id");
        verify(taskRepository, times(1)).save(taskEntity);
    }

    @Test
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        // Given
        when(taskRepository.findById("nonexistent-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TaskServiceException.class, () -> {
            taskService.deleteTask("nonexistent-id");
        });
        verify(taskRepository, times(1)).findById("nonexistent-id");
        verify(taskRepository, never()).save(any(TaskEntity.class));
    }

    @Test
    void createTask_ShouldSetCorrectStatusAndTimestamps_WhenCreatingTask() {
        // Given
        when(userRepository.findById("user-id")).thenReturn(Optional.of(userEntity));
        when(taskRepository.save(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity entity = invocation.getArgument(0);
            entity.setId("generated-task-id");
            return entity;
        });
        doNothing().when(taskPublisher).publishTask(any(Task.class));

        // When
        Task result = taskService.createTask(createTaskRequest);

        // Then
        assertEquals(TaskStatus.PENDING, result.getStatus());
        assertFalse(result.isDeleted());
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }
}