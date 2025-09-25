package com.alina.taskmanager.repository.adapter;

import com.alina.taskmanager.entity.TaskEntity;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.TaskStatus;
import com.alina.taskmanager.repository.TaskRepository;
import com.alina.taskmanager.repository.jpa.SpringTaskJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("db")
public class TaskRepositoryAdapter implements TaskRepository {
    
    private final SpringTaskJpaRepository jpaRepository;
    
    public TaskRepositoryAdapter(SpringTaskJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Task save(Task task) {
        TaskEntity entity = TaskEntity.fromModel(task);
        TaskEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toModel();
    }
    
    @Override
    public Optional<Task> findById(String id) {
        return jpaRepository.findById(id)
                .map(TaskEntity::toModel);
    }
    
    @Override
    public List<Task> findByUserId(String userId) {
        return jpaRepository.findByUserIdAndNotDeleted(userId)
                .stream()
                .map(TaskEntity::toModel)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findByUserIdAndStatusNotDeleted(String userId, TaskStatus status) {
        return jpaRepository.findByUserIdAndStatusAndNotDeleted(userId, status)
                .stream()
                .map(TaskEntity::toModel)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(String id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setDeleted(true);
            jpaRepository.save(entity);
        });
    }
}
