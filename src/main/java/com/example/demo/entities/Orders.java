package com.example.demo.entities;
import java.time.Instant;
import java.util.Date;

import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_info")
public class Orders
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int oId;
	private String oName;
	private double oPrice;
	private int oQuantity;
	private Date orderDate;
	private double totalAmmout;
	
	@ManyToOne
	@JoinColumn(name="user_u_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "restaurant_id")
	private Restaurant restaurant;

	@ManyToOne
	@JoinColumn(name = "driver_id")
	private Driver driver;

	@Enumerated(EnumType.STRING)
	private OrderStatus status = OrderStatus.PLACED;

	@Column(name = "delivery_location", columnDefinition = "geometry(Point,4326)")
	private Point deliveryLocation;

	@Column(name = "estimated_eta_at")
	private Instant estimatedEtaAt;

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public Point getDeliveryLocation() {
		return deliveryLocation;
	}

	public void setDeliveryLocation(Point deliveryLocation) {
		this.deliveryLocation = deliveryLocation;
	}

	public Instant getEstimatedEtaAt() {
		return estimatedEtaAt;
	}

	public void setEstimatedEtaAt(Instant estimatedEtaAt) {
		this.estimatedEtaAt = estimatedEtaAt;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	

	public int getoId() {
		return oId;
	}

	public void setoId(int oId) {
		this.oId = oId;
	}

	public String getoName() {
		return oName;
	}

	public void setoName(String oName) {
		this.oName = oName;
	}

	public double getoPrice() {
		return oPrice;
	}

	public void setoPrice(double oPrice) {
		this.oPrice = oPrice;
	}

	public int getoQuantity() {
		return oQuantity;
	}

	public void setoQuantity(int oQuantity) {
		this.oQuantity = oQuantity;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	

	public double getTotalAmmout() {
		return totalAmmout;
	}

	public void setTotalAmmout(double totalAmmout) {
		this.totalAmmout = totalAmmout;
	}

	@Override
	public String toString() {
		return "Orders [oId=" + oId + ", oName=" + oName + ", oPrice=" + oPrice + ", oQuantity=" + oQuantity
				+ ", orderDate=" + orderDate + ", totalAmmout=" + totalAmmout + ", user=" + user + "]";
	}


}