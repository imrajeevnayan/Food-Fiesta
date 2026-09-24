package com.example.demo.events;

public record OrderCreatedEvent(int orderId, double restaurantLon, double restaurantLat) {
}
