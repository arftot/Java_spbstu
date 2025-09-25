package com.alina.taskmanager.service;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.exception.ResourceNotFoundException;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.TaskRepository;
import com.alina.taskmanager.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private com.alina.taskmanager.service.impl.TaskServiceImpl taskService;

    private CreateTaskRequest createTaskRequest;
    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUsername("testuser");

        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setUserId("user-123");
        createTaskRequest.setTitle("Test Task");
        createTaskRequest.setDescription("Test Description");
        createTaskRequest.setDueDate(Instant.now().plusSeconds(3600));

        testTask = new Task();
        testTask.setId("task-123");
        testTask.setUserId("user-123");
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.PENDING);
        testTask.setCreatedAt(Instant.now());
        testTask.setDueDate(Instant.now().plusSeconds(3600));
    }

    @Test
    void createTask_ShouldReturnCreatedTask_WhenValidRequest() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.createTask(createTaskRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        verify(userRepository).findById("user-123");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById("nonexistent-user")).thenReturn(Optional.empty());

        createTaskRequest.setUserId("nonexistent-user");

        assertThatThrownBy(() -> taskService.createTask(createTaskRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getTasksByUser_ShouldReturnTasks_WhenUserExists() {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskRepository.findByUserId("user-123")).thenReturn(tasks);

        List<Task> result = taskService.getTasksByUser("user-123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Task");
        verify(taskRepository).findByUserId("user-123");
    }

    @Test
    void getTasksByUser_ShouldReturnEmptyList_WhenUserHasNoTasks() {
        when(taskRepository.findByUserId("user-123")).thenReturn(Arrays.asList());

        List<Task> result = taskService.getTasksByUser("user-123");

        assertThat(result).isEmpty();
        verify(taskRepository).findByUserId("user-123");
    }

    @Test
    void getPendingTasksByUser_ShouldReturnPendingTasks_WhenUserExists() {
        List<Task> pendingTasks = Arrays.asList(testTask);
        when(taskRepository.findByUserIdAndStatusNotDeleted("user-123", TaskStatus.PENDING))
                .thenReturn(pendingTasks);

        List<Task> result = taskService.getPendingTasksByUser("user-123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.PENDING);
        verify(taskRepository).findByUserIdAndStatusNotDeleted("user-123", TaskStatus.PENDING);
    }

    @Test
    void deleteTask_ShouldCallRepository_WhenTaskExists() {
        taskService.deleteTask("task-123");

        verify(taskRepository).deleteById("task-123");
    }

    @Test
    void createTask_ShouldSetCorrectFields_WhenValidRequest() {
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId("task-123");
            return task;
        });

        Task result = taskService.createTask(createTaskRequest);

        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getTitle()).isEqualTo("Test Task");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();
    }
}
