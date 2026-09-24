package com.example.demo.services;

import com.example.demo.config.KafkaTopicConfig;
import com.example.demo.entities.Driver;
import com.example.demo.entities.OrderStatus;
import com.example.demo.entities.Orders;
import com.example.demo.events.OrderCreatedEvent;
import com.example.demo.repositories.DriverGeoRepository;
import com.example.demo.repositories.DriverRepository;
import com.example.demo.repositories.OrderRepository;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderRoutingConsumer {

    private static final double SEARCH_RADIUS_METERS = 5_000;

    private final DriverGeoRepository driverGeoRepository;
    private final DriverRepository driverRepository;
    private final OrderRepository orderRepository;
    private final OrderStateService orderStateService;

    public OrderRoutingConsumer(DriverGeoRepository driverGeoRepository, DriverRepository driverRepository,
            OrderRepository orderRepository, OrderStateService orderStateService) {
        this.driverGeoRepository = driverGeoRepository;
        this.driverRepository = driverRepository;
        this.orderRepository = orderRepository;
        this.orderStateService = orderStateService;
    }

    @KafkaListener(topics = KafkaTopicConfig.NEW_ORDER_CREATED_TOPIC)
    @Transactional
    public void assignNearestDriver(OrderCreatedEvent event) {
        Orders order = orderRepository.findById(event.orderId()).orElseThrow(
                () -> new IllegalArgumentException("Order not found: " + event.orderId()));
        if (order.getStatus() != OrderStatus.PLACED || order.getDriver() != null) {
            return;
        }

        for (GeoResult<GeoLocation<String>> result : driverGeoRepository.findNearby(
                event.restaurantLon(), event.restaurantLat(), SEARCH_RADIUS_METERS)) {
            long driverId = Long.parseLong(result.getContent().getName());
            Driver driver = driverRepository.findByIdForUpdate(driverId).orElse(null);
            if (driver == null || driver.getStatus() != Driver.Status.AVAILABLE) {
                continue;
            }

            driver.setStatus(Driver.Status.ASSIGNED);
            driverRepository.save(driver);
            order.setDriver(driver);
            orderStateService.transition(order.getoId(), OrderStatus.CONFIRMED);
            return;
        }
    }
}
