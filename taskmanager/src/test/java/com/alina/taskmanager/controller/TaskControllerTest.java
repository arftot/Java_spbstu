package com.alina.taskmanager.controller;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@SuppressWarnings("deprecation")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;
    private CreateTaskRequest createTaskRequest;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId("task-123");
        testTask.setUserId("user-123");
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.PENDING);
        testTask.setCreatedAt(Instant.now());
        testTask.setDueDate(Instant.now().plusSeconds(3600));

        createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setUserId("user-123");
        createTaskRequest.setTitle("Test Task");
        createTaskRequest.setDescription("Test Description");
        createTaskRequest.setDueDate(Instant.now().plusSeconds(3600));
    }

    @Test
    void createTask_ShouldReturnCreatedTask_WhenValidRequest() throws Exception {
        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(testTask);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("task-123"))
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.dueDate").exists());
    }

    @Test
    void createTask_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        CreateTaskRequest invalidRequest = new CreateTaskRequest();
        invalidRequest.setUserId("user-123");
        invalidRequest.setTitle(""); // Empty title should fail validation
        invalidRequest.setDueDate(Instant.now().minusSeconds(3600)); // Past date should fail validation

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void getTasks_ShouldReturnTasks_WhenUserExists() throws Exception {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByUser("user-123")).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks")
                        .param("userId", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("task-123"))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    void getPendingTasks_ShouldReturnPendingTasks_WhenUserExists() throws Exception {
        List<Task> pendingTasks = Arrays.asList(testTask);
        when(taskService.getPendingTasksByUser("user-123")).thenReturn(pendingTasks);

        mockMvc.perform(get("/api/tasks/pending")
                        .param("userId", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("task-123"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void deleteTask_ShouldReturnNoContent_WhenTaskExists() throws Exception {
        mockMvc.perform(delete("/api/tasks/task-123"))
                .andExpect(status().isNoContent());
    }
}
