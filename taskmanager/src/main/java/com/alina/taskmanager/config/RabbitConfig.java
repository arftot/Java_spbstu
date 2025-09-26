package com.alina.taskmanager.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String TASK_QUEUE = "task-queue";
    public static final String TASK_EXCHANGE = "task-exchange";

    @Bean
    public Queue taskQueue() {
        return new Queue(TASK_QUEUE, true);
    }

    @Bean
    public TopicExchange taskExchange() {
        return new TopicExchange(TASK_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue taskQueue, TopicExchange taskExchange) {
        return BindingBuilder.bind(taskQueue).to(taskExchange).with("task.created");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
