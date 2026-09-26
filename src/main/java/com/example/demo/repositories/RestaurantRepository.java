package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.Restaurant;

public interface RestaurantRepository extends CrudRepository<Restaurant, Long> {

    /**
     * Nearby-restaurant search (Phase 5). Uses PostGIS ST_DWithin on the
     * geography cast so the radius is in meters, ordered by true distance.
     */
    @Query(value = """
            SELECT * FROM restaurant r
            WHERE r.is_active
              AND ST_DWithin(r.location::geography,
                             ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                             :radiusMeters)
            ORDER BY ST_Distance(r.location::geography,
                                 ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography)
            """, nativeQuery = true)
    List<Restaurant> findNearby(@Param("lon") double lon,
                                @Param("lat") double lat,
                                @Param("radiusMeters") double radiusMeters);

    /**
     * Same radius search, but also projects the true ground distance in meters
     * so callers can show "1.2 km away" without a second round-trip.
     */
    @Query(value = """
            SELECT r.id AS id,
                   r.name AS name,
                   r.address AS address,
                   r.avg_prep_minutes AS avgPrepMinutes,
                   ST_Distance(r.location::geography,
                               ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) AS distanceMeters
            FROM restaurant r
            WHERE r.is_active
              AND ST_DWithin(r.location::geography,
                             ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                             :radiusMeters)
            ORDER BY distanceMeters
            """, nativeQuery = true)
    List<NearbyRestaurantProjection> findNearbyWithDistance(@Param("lon") double lon,
                                                            @Param("lat") double lat,
                                                            @Param("radiusMeters") double radiusMeters);
}
