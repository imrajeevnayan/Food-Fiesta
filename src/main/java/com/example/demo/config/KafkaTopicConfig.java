package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String NEW_ORDER_CREATED_TOPIC = "new-order-created";

    @Bean
    public NewTopic newOrderCreatedTopic() {
        return TopicBuilder.name(NEW_ORDER_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
