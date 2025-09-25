package com.alina.taskmanager.integration;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.dto.CreateUserRequest;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.service.TaskService;
import com.alina.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("db")
@Transactional
class TaskServiceDbIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    private User testUser;
    private CreateTaskRequest createTaskRequest;

    @BeforeEach
    void setUp() {
        // Create a test user first
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("testuser");
        testUser = userService.register(userRequest);

        // Create task request
        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setUserId(testUser.getId());
        createTaskRequest.setTitle("Integration Test Task");
        createTaskRequest.setDescription("Test task for integration testing");
        createTaskRequest.setDueDate(Instant.now().plusSeconds(3600));
    }

    @Test
    void createTask_ShouldSaveToDatabase_WhenValidRequest() {
        Task createdTask = taskService.createTask(createTaskRequest);

        assertThat(createdTask).isNotNull();
        assertThat(createdTask.getId()).isNotBlank();
        assertThat(createdTask.getTitle()).isEqualTo("Integration Test Task");
        assertThat(createdTask.getUserId()).isEqualTo(testUser.getId());
    }

    @Test
    void getTasksByUser_ShouldReturnSavedTasks_WhenUserHasTasks() {
        // Create a task
        Task createdTask = taskService.createTask(createTaskRequest);

        // Retrieve tasks for the user
        List<Task> userTasks = taskService.getTasksByUser(testUser.getId());

        assertThat(userTasks).hasSize(1);
        assertThat(userTasks.get(0).getTitle()).isEqualTo("Integration Test Task");
        assertThat(userTasks.get(0).getId()).isEqualTo(createdTask.getId());
    }

    @Test
    void getPendingTasksByUser_ShouldReturnPendingTasks_WhenUserHasPendingTasks() {
        // Create a task
        taskService.createTask(createTaskRequest);

        // Retrieve pending tasks for the user
        List<Task> pendingTasks = taskService.getPendingTasksByUser(testUser.getId());

        assertThat(pendingTasks).hasSize(1);
        assertThat(pendingTasks.get(0).getTitle()).isEqualTo("Integration Test Task");
    }

    @Test
    void deleteTask_ShouldMarkAsDeleted_WhenTaskExists() {
        // Create a task
        Task createdTask = taskService.createTask(createTaskRequest);

        // Delete the task
        taskService.deleteTask(createdTask.getId());

        // Verify task is marked as deleted (should not appear in regular queries)
        List<Task> userTasks = taskService.getTasksByUser(testUser.getId());
        assertThat(userTasks).isEmpty();
    }
}
