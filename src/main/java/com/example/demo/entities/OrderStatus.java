package com.example.demo.entities;

/** Order lifecycle states. Mirrors the CHECK constraint on order_info.status. */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
