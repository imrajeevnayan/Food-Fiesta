package com.example.demo.dto;

import java.time.Instant;

public record DeliveryEstimate(int orderId, double distanceMeters, int preparationMinutes,
        int trafficDelayMinutes, int deliveryMinutes, Instant estimatedArrivalAt, String source) {
}
