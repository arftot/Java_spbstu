package com.alina.taskmanager.repository.inmemory;

import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.repository.TaskRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Profile("inmemory")
public class InMemoryTaskRepository implements TaskRepository {
    private final Map<String, Task> storage = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        storage.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Task> findByUserId(String userId) {
        return storage.values().stream()
                .filter(t -> userId.equals(t.getUserId()))
                .filter(t -> !t.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByUserIdAndStatusNotDeleted(String userId, TaskStatus status) {
        return storage.values().stream()
                .filter(t -> userId.equals(t.getUserId()))
                .filter(t -> !t.isDeleted())
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        Task task = storage.get(id);
        if (task != null) {
            task.setDeleted(true);
            storage.put(id, task);
        }
    }
}


