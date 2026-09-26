package com.example.demo.services;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

/**
 * External routing provider (OSRM-compatible). Every call is guarded by a
 * Resilience4j circuit breaker so a slow or failing map vendor cannot cascade
 * into the order/ETA path; on failure we degrade to the straight-line estimate.
 */
@Component
public class MapRoutingClient {

    private static final Logger log = LoggerFactory.getLogger(MapRoutingClient.class);

    private final RestClient restClient;

    public MapRoutingClient(@Value("${foodfiesta.map.base-url:https://router.project-osrm.org}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    /** Road travel time in whole minutes between two WGS84 points, if the vendor answers. */
    @CircuitBreaker(name = "mapRouting", fallbackMethod = "travelFallback")
    public Optional<Integer> travelMinutes(double fromLon, double fromLat, double toLon, double toLat) {
        String path = String.format(Locale.ROOT, "/route/v1/driving/%f,%f;%f,%f?overview=false",
                fromLon, fromLat, toLon, toLat);
        OsrmsResponse response = restClient.get().uri(path).retrieve().body(OsrmsResponse.class);
        if (response == null || response.routes() == null || response.routes().isEmpty()) {
            return Optional.empty();
        }
        double seconds = response.routes().get(0).duration();
        return Optional.of(Math.max(1, (int) Math.ceil(seconds / 60.0)));
    }

    @SuppressWarnings("unused")
    private Optional<Integer> travelFallback(double fromLon, double fromLat, double toLon, double toLat, Throwable t) {
        log.warn("Map routing unavailable ({}); falling back to straight-line travel time", t.getMessage());
        return Optional.empty();
    }

    record OsrmsResponse(List<Route> routes) {
    }

    record Route(double duration) {
    }
}
