package com.example.demo.services;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.OrderStatus;
import com.example.demo.entities.Orders;
import com.example.demo.repositories.OrderRepository;

/**
 * Enforces the order lifecycle:
 * PLACED -> CONFIRMED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED.
 * Any non-terminal state may transition to CANCELLED.
 */
@Service
public class OrderStateService {

    public static final String TOPIC_PREFIX = "/topic/orders/";

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);
    static {
        ALLOWED.put(OrderStatus.PLACED, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.PREPARING, Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.DELIVERED, Set.of());
        ALLOWED.put(OrderStatus.CANCELLED, Set.of());
    }

    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messaging;

    public OrderStateService(OrderRepository orderRepository, SimpMessagingTemplate messaging) {
        this.orderRepository = orderRepository;
        this.messaging = messaging;
    }

    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    @Transactional
    public Orders transition(int orderId, OrderStatus target) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        OrderStatus current = order.getStatus();
        if (!canTransition(current, target)) {
            throw new IllegalStateException(
                    "Illegal transition " + current + " -> " + target + " for order " + orderId);
        }
        order.setStatus(target);
        Orders saved = orderRepository.save(order);
        messaging.convertAndSend(TOPIC_PREFIX + orderId, target);
        return saved;
    }
}
