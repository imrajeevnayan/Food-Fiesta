package com.example.demo.controllers;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DeliveryLocation;
import com.example.demo.services.DeliveryEstimateService;

@RestController
@RequestMapping("/api/orders")
public class OrderDeliveryLocationController {

    private final DeliveryEstimateService deliveryEstimateService;

    public OrderDeliveryLocationController(DeliveryEstimateService deliveryEstimateService) {
        this.deliveryEstimateService = deliveryEstimateService;
    }

    @PutMapping("/{orderId}/delivery-location")
    public ResponseEntity<Void> updateDeliveryLocation(@PathVariable int orderId,
            @RequestBody DeliveryLocation location) {
        if (!location.isValid()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            deliveryEstimateService.updateDeliveryLocation(orderId, location);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
