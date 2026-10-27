
package com.foodie.service;

import com.foodie.model.Order;
import com.foodie.model.OrderItem;
import com.foodie.model.Restaurant;
import com.foodie.repository.OrderItemRepository;
import com.foodie.repository.OrderRepository;
import com.foodie.repository.RestaurantRepository;
import com.foodie.dto.MerchantDashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.math.BigDecimal;

@Service
public class OrderService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "NEW",
            "PREPARING",
            "READY",
            "ON_DELIVERY",
            "DELIVERED",
            "CANCELLED"
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            RestaurantRepository restaurantRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders(Long merchantId, Long restaurantId) {
        verifyRestaurantOwnership(merchantId, restaurantId);

        return orderRepository
                .findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long merchantId, Long restaurantId, Long orderId) {
        verifyRestaurantOwnership(merchantId, restaurantId);

        return orderRepository
                .findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() ->
                        new NoSuchElementException("Order not found"));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItems(
            Long merchantId, Long restaurantId, Long orderId) {

        getOrder(merchantId, restaurantId, orderId);

        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional
    public Order updateOrderStatus(
            Long merchantId,
            Long restaurantId,
            Long orderId,
            String newStatus) {

        if (newStatus == null ||
                !VALID_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid order status");
        }

        Order order = getOrder(merchantId, restaurantId, orderId);

        if ("DELIVERED".equals(order.getStatus()) ||
                "CANCELLED".equals(order.getStatus())) {
            throw new IllegalArgumentException(
                    "Cannot update a completed or cancelled order");
        }


        String currentStatus = order.getStatus();

        boolean validTransition = switch (currentStatus) {
            case "NEW" ->
                    "PREPARING".equals(newStatus)
                            || "CANCELLED".equals(newStatus);

            case "PREPARING" ->
                    "READY".equals(newStatus)
                            || "CANCELLED".equals(newStatus);

            case "READY" ->
                    "ON_DELIVERY".equals(newStatus)
                            || "CANCELLED".equals(newStatus);

            case "ON_DELIVERY" ->
                    "DELIVERED".equals(newStatus);

            default -> false;
        };

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid order status transition: "
                            + currentStatus + " -> " + newStatus);
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);

    }

    @Transactional(readOnly = true)
    public long countOrders(Long merchantId, Long restaurantId) {
        verifyRestaurantOwnership(merchantId, restaurantId);

        return orderRepository.countByRestaurantId(restaurantId);
    }

    private Restaurant verifyRestaurantOwnership(
            Long merchantId, Long restaurantId) {

        return restaurantRepository.findByMerchantId(merchantId)
                .stream()
                .filter(restaurant ->
                        restaurant.getId().equals(restaurantId))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException("Restaurant not found"));
    }


    @Transactional(readOnly = true)
    public MerchantDashboardResponse getDashboardStats(
            Long merchantId, Long restaurantId) {

        verifyRestaurantOwnership(merchantId, restaurantId);

        List<Order> orders = orderRepository
                .findByRestaurantIdOrderByCreatedAtDesc(restaurantId);

        long newOrders = 0;
        long preparingOrders = 0;
        long readyOrders = 0;
        long onDeliveryOrders = 0;
        long deliveredOrders = 0;
        long cancelledOrders = 0;

        BigDecimal deliveredRevenue = BigDecimal.ZERO;

        for (Order order : orders) {
            switch (order.getStatus()) {
                case "NEW" -> newOrders++;
                case "PREPARING" -> preparingOrders++;
                case "READY" -> readyOrders++;
                case "ON_DELIVERY" -> onDeliveryOrders++;
                case "DELIVERED" -> {
                    deliveredOrders++;
                    deliveredRevenue = deliveredRevenue.add(
                            order.getTotalAmount());
                }
                case "CANCELLED" -> cancelledOrders++;
                default -> {
                    // Unknown statuses are not counted in a category.
                }
            }
        }

        return new MerchantDashboardResponse(
                orders.size(),
                newOrders,
                preparingOrders,
                readyOrders,
                onDeliveryOrders,
                deliveredOrders,
                cancelledOrders,
                deliveredRevenue
        );
    }


}
