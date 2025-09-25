package com.alina.taskmanager.controller;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getTasks(@RequestParam String userId) {
        return taskService.getTasksByUser(userId);
    }

    @GetMapping("/pending")
    public List<Task> getPending(@RequestParam String userId) {
        return taskService.getPendingTasksByUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.createTask(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
    }
}


