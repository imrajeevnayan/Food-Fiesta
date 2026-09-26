package com.example.demo.dto;

public record NearbyRestaurant(Long id, String name, String address, int avgPrepMinutes, long distanceMeters) {
}
