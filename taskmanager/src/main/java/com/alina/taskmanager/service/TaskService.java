package com.alina.taskmanager.service;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.exception.TaskServiceException;
import com.alina.taskmanager.model.Task;

import java.util.List;

public interface TaskService {
    Task createTask(CreateTaskRequest request) throws TaskServiceException;
    List<Task> getTasksByUser(String userId) throws TaskServiceException;
    List<Task> getPendingTasksByUser(String userId) throws TaskServiceException;
    void deleteTask(String id) throws TaskServiceException;
    List<Task> getOverdueTasks() throws TaskServiceException;
    void markTaskAsOverdue(String taskId) throws TaskServiceException;
}


