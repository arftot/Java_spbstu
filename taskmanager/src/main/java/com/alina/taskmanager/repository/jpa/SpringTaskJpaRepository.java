package com.alina.taskmanager.repository.jpa;

import com.alina.taskmanager.entity.TaskEntity;
import com.alina.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringTaskJpaRepository extends JpaRepository<TaskEntity, String> {
    
    @Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId AND t.deleted = false")
    List<TaskEntity> findByUserIdAndNotDeleted(@Param("userId") String userId);
    
    @Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId AND t.status = :status AND t.deleted = false")
    List<TaskEntity> findByUserIdAndStatusAndNotDeleted(@Param("userId") String userId, @Param("status") TaskStatus status);
}
