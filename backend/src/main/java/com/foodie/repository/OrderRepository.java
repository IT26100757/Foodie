
package com.foodie.repository;

import com.foodie.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    Optional<Order> findByIdAndRestaurantId(Long orderId, Long restaurantId);

    long countByRestaurantId(Long restaurantId);
}
