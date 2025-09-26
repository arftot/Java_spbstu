package com.alina.taskmanager.repository;

import com.alina.taskmanager.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskJpaRepository extends JpaRepository<TaskEntity, String> {
    
    List<TaskEntity> findByUserIdAndDeletedFalse(String userId);
    
    List<TaskEntity> findByUserId(String userId);
    
    @Query("SELECT t FROM TaskEntity t WHERE t.dueDate < :now AND t.status = 'PENDING' AND t.deleted = false AND t.overdue = false")
    List<TaskEntity> findOverdueTasks(@Param("now") LocalDateTime now);
}
