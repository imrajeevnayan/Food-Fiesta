package com.example.demo.repositories;

public interface NearbyRestaurantProjection {

    Long getId();

    String getName();

    String getAddress();

    Integer getAvgPrepMinutes();

    Double getDistanceMeters();
}
