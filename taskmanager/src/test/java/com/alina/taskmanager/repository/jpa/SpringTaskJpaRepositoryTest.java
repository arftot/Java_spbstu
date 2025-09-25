package com.alina.taskmanager.repository.jpa;

import com.alina.taskmanager.entity.TaskEntity;
import com.alina.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("db")
class SpringTaskJpaRepositoryTest {

    @Autowired
    private SpringTaskJpaRepository repository;

    private TaskEntity testTask;

    @BeforeEach
    void setUp() {
        testTask = new TaskEntity();
        testTask.setUserId("user-123");
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.PENDING);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setDeleted(false);
    }

    @Test
    void save_ShouldReturnSavedTask() {
        TaskEntity saved = repository.save(testTask);

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getTitle()).isEqualTo("Test Task");
        assertThat(saved.getUserId()).isEqualTo("user-123");
    }

    @Test
    void findById_ShouldReturnTask_WhenExists() {
        TaskEntity saved = repository.save(testTask);

        Optional<TaskEntity> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Task");
    }

    @Test
    void findByUserIdAndNotDeleted_ShouldReturnTasks_WhenUserHasTasks() {
        repository.save(testTask);

        List<TaskEntity> tasks = repository.findByUserIdAndNotDeleted("user-123");

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Test Task");
    }

    @Test
    void findByUserIdAndStatusAndNotDeleted_ShouldReturnPendingTasks() {
        repository.save(testTask);

        List<TaskEntity> tasks = repository.findByUserIdAndStatusAndNotDeleted("user-123", TaskStatus.PENDING);

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getStatus()).isEqualTo(TaskStatus.PENDING);
    }
}
