package com.alina.taskmanager.messaging;

import com.alina.taskmanager.model.Task;
import com.alina.taskmanager.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskPublisher {
    private final RabbitTemplate rabbitTemplate;

    public TaskPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTask(Task task) {
        rabbitTemplate.convertAndSend(RabbitConfig.TASK_EXCHANGE, "task.created", task);
    }
}
