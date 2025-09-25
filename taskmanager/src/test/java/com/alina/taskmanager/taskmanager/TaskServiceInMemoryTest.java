package com.alina.taskmanager.taskmanager;

import com.alina.taskmanager.dto.CreateTaskRequest;
import com.alina.taskmanager.dto.CreateUserRequest;
import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.model.User;
import com.alina.taskmanager.service.TaskService;
import com.alina.taskmanager.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("inmemory")
public class TaskServiceInMemoryTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Test
    void createAndFetchTask() {
        CreateUserRequest userReq = new CreateUserRequest();
        userReq.setUsername("alice");
        User user = userService.register(userReq);

        CreateTaskRequest req = new CreateTaskRequest();
        req.setUserId(user.getId());
        req.setTitle("Test Task");
        req.setDescription("Desc");
        req.setDueDate(Instant.now());

        Task created = taskService.createTask(req);
        assertThat(created.getId()).isNotBlank();

        List<Task> tasks = taskService.getTasksByUser(user.getId());
        assertThat(tasks).isNotEmpty();
        assertThat(tasks.get(0).getTitle()).isEqualTo("Test Task");
    }
}


