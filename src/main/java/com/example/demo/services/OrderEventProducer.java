package com.example.demo.services;

import com.example.demo.config.KafkaTopicConfig;
import com.example.demo.events.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OrderCreatedEvent event) {
        kafkaTemplate.send(KafkaTopicConfig.NEW_ORDER_CREATED_TOPIC, String.valueOf(event.orderId()), event);
    }
}
