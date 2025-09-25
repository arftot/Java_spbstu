package com.alina.taskmanager.service.impl;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.exception.ResourceNotFoundException;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.repository.TaskRepository;
import com.alina.taskmanager.repository.UserRepository;
import com.alina.taskmanager.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Task createTask(CreateTaskRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
        Task task = new Task();
        task.setUserId(user.getId());
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(Instant.now());
        return taskRepository.save(task);
    }

    @Override
    public List<Task> getTasksByUser(String userId) {
        return taskRepository.findByUserId(userId);
    }

    @Override
    public List<Task> getPendingTasksByUser(String userId) {
        return taskRepository.findByUserIdAndStatusNotDeleted(userId, TaskStatus.PENDING);
    }

    @Override
    public void deleteTask(String id) {
        taskRepository.deleteById(id);
    }
}


