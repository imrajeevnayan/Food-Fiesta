package com.example.demo.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.DeliveryEstimate;
import com.example.demo.dto.DeliveryLocation;
import com.example.demo.entities.Orders;
import com.example.demo.repositories.OrderRepository;

@Service
public class DeliveryEstimateService {

    private static final GeometryFactory GEO = new GeometryFactory(new PrecisionModel(), 4326);
    private static final Pattern WHOLE_NUMBER = Pattern.compile("\\d+");
    private static final double DELIVERY_SPEED_METERS_PER_MINUTE = 400;

    private final OrderRepository orderRepository;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    public DeliveryEstimateService(OrderRepository orderRepository,
            ObjectProvider<ChatClient.Builder> chatClientBuilderProvider) {
        this.orderRepository = orderRepository;
        this.chatClientBuilderProvider = chatClientBuilderProvider;
    }

    @Transactional
    public void updateDeliveryLocation(int orderId, DeliveryLocation location) {
        Orders order = findOrder(orderId);
        order.setDeliveryLocation(GEO.createPoint(new org.locationtech.jts.geom.Coordinate(location.lon(), location.lat())));
        orderRepository.save(order);
    }

    @Transactional
    public DeliveryEstimate estimate(int orderId) {
        Orders order = findOrder(orderId);
        if (order.getDeliveryLocation() == null) {
            throw new IllegalStateException("Delivery location is required before an ETA can be calculated");
        }

        Double distanceMeters = orderRepository.findDeliveryDistanceMetersById(orderId);
        if (distanceMeters == null) {
            throw new IllegalStateException("Restaurant location is required before an ETA can be calculated");
        }

        int preparationMinutes = order.getRestaurant().getAvgPrepMinutes();
        int trafficDelayMinutes = mockedTrafficDelayMinutes(orderId);
        int travelMinutes = Math.max(1, (int) Math.ceil(distanceMeters / DELIVERY_SPEED_METERS_PER_MINUTE));
        int heuristicMinutes = preparationMinutes + travelMinutes + trafficDelayMinutes;
        Integer aiMinutes = predictWithAi(distanceMeters, preparationMinutes, trafficDelayMinutes, travelMinutes);
        int deliveryMinutes = aiMinutes == null ? heuristicMinutes : boundedPrediction(aiMinutes, heuristicMinutes);
        String source = aiMinutes == null ? "heuristic" : "spring-ai";
        Instant estimatedArrivalAt = Instant.now().plus(deliveryMinutes, ChronoUnit.MINUTES);

        order.setEstimatedEtaAt(estimatedArrivalAt);
        orderRepository.save(order);
        return new DeliveryEstimate(orderId, distanceMeters, preparationMinutes, trafficDelayMinutes,
                deliveryMinutes, estimatedArrivalAt, source);
    }

    private Orders findOrder(int orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
    }

    private int mockedTrafficDelayMinutes(int orderId) {
        return 5 + Math.floorMod(orderId, 11);
    }

    private Integer predictWithAi(double distanceMeters, int preparationMinutes, int trafficDelayMinutes,
            int travelMinutes) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            return null;
        }

        try {
            String response = builder.build().prompt()
                    .system("You estimate food delivery duration. Return exactly one whole-number total ETA in minutes.")
                    .user("""
                            Restaurant preparation time: %d minutes
                            Delivery distance: %.0f meters
                            Travel time at current conditions: %d minutes
                            Mocked traffic delay: %d minutes
                            Return only the total ETA in minutes.
                            """.formatted(preparationMinutes, distanceMeters, travelMinutes, trafficDelayMinutes))
                    .call()
                    .content();
            Matcher matcher = WHOLE_NUMBER.matcher(response == null ? "" : response);
            return matcher.find() ? Integer.parseInt(matcher.group()) : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private int boundedPrediction(int predictedMinutes, int heuristicMinutes) {
        int minimum = Math.max(1, heuristicMinutes - 10);
        int maximum = Math.min(180, heuristicMinutes + 30);
        return Math.max(minimum, Math.min(maximum, predictedMinutes));
    }
}
