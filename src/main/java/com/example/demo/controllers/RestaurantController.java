package com.example.demo.controllers;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.NearbyRestaurant;
import com.example.demo.repositories.RestaurantRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/restaurants")
@Tag(name = "Restaurant Controller", description = "Geospatial restaurant search")
public class RestaurantController {

    private static final Pattern RADIUS = Pattern.compile("^([0-9]*\\.?[0-9]+)\\s*(km|m)?$", Pattern.CASE_INSENSITIVE);
    private static final double DEFAULT_RADIUS_METERS = 5_000;
    private static final double MAX_RADIUS_METERS = 50_000;

    private final RestaurantRepository restaurantRepository;

    public RestaurantController(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/nearby")
    @Operation(summary = "Nearby restaurants",
            description = "PostGIS ST_DWithin radius search returning active restaurants ordered by distance.")
    public ResponseEntity<List<NearbyRestaurant>> nearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false) String radius) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            return ResponseEntity.badRequest().build();
        }
        double radiusMeters = parseRadiusMeters(radius);
        if (radiusMeters <= 0) {
            return ResponseEntity.badRequest().build();
        }
        List<NearbyRestaurant> body = restaurantRepository.findNearbyWithDistance(lon, lat, radiusMeters).stream()
                .map(p -> new NearbyRestaurant(p.getId(), p.getName(), p.getAddress(),
                        p.getAvgPrepMinutes(), Math.round(p.getDistanceMeters())))
                .toList();
        return ResponseEntity.ok(body);
    }

    private static double parseRadiusMeters(String radius) {
        if (radius == null || radius.isBlank()) {
            return DEFAULT_RADIUS_METERS;
        }
        Matcher matcher = RADIUS.matcher(radius.trim().toLowerCase(Locale.ROOT).replace(" ", ""));
        if (!matcher.matches()) {
            return -1;
        }
        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2);
        if (unit == null || unit.equals("km")) {
            value *= 1_000;
        }
        return Math.min(value, MAX_RADIUS_METERS);
    }
}
