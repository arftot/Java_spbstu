package com.alina.taskmanager.repository;

import com.alina.taskmanager.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskJpaRepository extends JpaRepository<TaskEntity, String> {
    
    List<TaskEntity> findByUserIdAndDeletedFalse(String userId);
    
    List<TaskEntity> findByUserId(String userId);
}
