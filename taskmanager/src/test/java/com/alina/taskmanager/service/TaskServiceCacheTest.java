package com.alina.taskmanager.service;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.entity.TaskEntity;
import com.alina.taskmanager.entity.UserEntity;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceCacheTest {

    @Mock
    private TaskJpaRepository taskRepository;

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UserEntity testUser;
    private TaskEntity testTask1;
    private TaskEntity testTask2;
    private String userId = "user-123";

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(userId);
        testUser.setUsername("testuser");

        testTask1 = new TaskEntity();
        testTask1.setId("task-1");
        testTask1.setUserId(userId);
        testTask1.setTitle("Task 1");
        testTask1.setStatus(TaskStatus.PENDING);
        testTask1.setDeleted(false);

        testTask2 = new TaskEntity();
        testTask2.setId("task-2");
        testTask2.setUserId(userId);
        testTask2.setTitle("Task 2");
        testTask2.setStatus(TaskStatus.IN_PROGRESS);
        testTask2.setDeleted(false);
    }

    @Test
    void getTasksByUser_ShouldReturnTasks() {
        // Given
        List<TaskEntity> tasks = Arrays.asList(testTask1, testTask2);
        when(taskRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(tasks);

        // When
        List<Task> result = taskService.getTasksByUser(userId);

        // Then
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findByUserIdAndDeletedFalse(userId);
    }

    @Test
    void getPendingTasksByUser_ShouldReturnOnlyPendingTasks() {
        // Given
        List<TaskEntity> tasks = Arrays.asList(testTask1, testTask2);
        when(taskRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(tasks);

        // When
        List<Task> result = taskService.getPendingTasksByUser(userId);

        // Then
        assertEquals(1, result.size()); // Only PENDING task
        assertEquals("Task 1", result.get(0).getTitle());
        verify(taskRepository, times(1)).findByUserIdAndDeletedFalse(userId);
    }

    @Test
    void createTask_ShouldCreateTask() {
        // Given
        CreateTaskRequest request = new CreateTaskRequest();
        request.setUserId(userId);
        request.setTitle("New Task");
        request.setDescription("New Description");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(testTask1);

        // When
        Task result = taskService.createTask(request);

        // Then
        assertNotNull(result);
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }

    @Test
    void deleteTask_ShouldMarkTaskAsDeleted() {
        // Given
        String taskId = "task-1";
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(testTask1);

        // When
        taskService.deleteTask(taskId);

        // Then
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
        assertTrue(testTask1.isDeleted());
    }

    @Test
    void getTasksByUser_EmptyResult_ShouldReturnEmptyList() {
        // Given
        when(taskRepository.findByUserIdAndDeletedFalse(userId)).thenReturn(Arrays.asList());

        // When
        List<Task> result = taskService.getTasksByUser(userId);

        // Then
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findByUserIdAndDeletedFalse(userId);
    }
}
