package com.example.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.Orders;
import com.example.demo.entities.User;

public interface OrderRepository extends JpaRepository<Orders, Integer>
{
	List<Orders> findOrdersByUser(User user);

	@Query(value = """
			SELECT ST_Distance(r.location::geography, o.delivery_location::geography)
			FROM order_info o
			JOIN restaurant r ON r.id = o.restaurant_id
			WHERE o.o_id = :orderId
			""", nativeQuery = true)
	Double findDeliveryDistanceMetersById(@Param("orderId") int orderId);
}