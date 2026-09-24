package com.example.demo.controllers;

import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DeliveryEstimate;
import com.example.demo.services.DeliveryEstimateService;

@RestController
@RequestMapping("/estimate-delivery")
public class DeliveryEstimateController {

    private final DeliveryEstimateService deliveryEstimateService;

    public DeliveryEstimateController(DeliveryEstimateService deliveryEstimateService) {
        this.deliveryEstimateService = deliveryEstimateService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<DeliveryEstimate> estimate(@PathVariable int orderId) {
        try {
            return ResponseEntity.ok(deliveryEstimateService.estimate(orderId));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).build();
        }
    }
}
