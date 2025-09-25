package com.alina.taskmanager.repository;

import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(String id);
    List<Task> findByUserId(String userId);
    List<Task> findByUserIdAndStatusNotDeleted(String userId, TaskStatus status);
    void deleteById(String id);
}


