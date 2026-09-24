package com.example.demo.dto;

public record DeliveryLocation(double lon, double lat) {

    public boolean isValid() {
        return lon >= -180 && lon <= 180 && lat >= -90 && lat <= 90;
    }
}
