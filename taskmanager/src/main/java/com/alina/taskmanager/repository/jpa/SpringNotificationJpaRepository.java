package com.alina.taskmanager.repository.jpa;

import com.alina.taskmanager.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringNotificationJpaRepository extends JpaRepository<NotificationEntity, String> {
    
    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.deleted = false")
    List<NotificationEntity> findByUserIdAndNotDeleted(@Param("userId") String userId);
    
    @Query("SELECT n FROM NotificationEntity n WHERE n.userId = :userId AND n.read = false AND n.deleted = false")
    List<NotificationEntity> findPendingByUserIdAndNotDeleted(@Param("userId") String userId);
}
