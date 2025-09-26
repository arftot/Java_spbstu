package com.alina.taskmanager.service;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.model.Task;

import java.util.List;

public interface TaskService {
    Task createTask(CreateTaskRequest request);
    List<Task> getTasksByUser(String userId);
    List<Task> getPendingTasksByUser(String userId);
    void deleteTask(String id);
    List<Task> getOverdueTasks();
    void markTaskAsOverdue(String taskId);
}


