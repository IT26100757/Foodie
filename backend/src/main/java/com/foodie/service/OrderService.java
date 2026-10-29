
package com.foodie.service;

import com.foodie.dto.DailyPerformanceResponse;
import com.foodie.dto.MerchantPerformanceResponse;
import com.foodie.dto.TopSellingItemResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.math.RoundingMode;
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
import java.util.HashMap;
import java.util.Comparator;

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


    @Transactional(readOnly = true)
    public MerchantPerformanceResponse getBusinessPerformance(
            Long merchantId, Long restaurantId, int days) {

        if (days != 7 && days != 30) {
            throw new IllegalArgumentException(
                    "Days must be 7 or 30");
        }

        verifyRestaurantOwnership(merchantId, restaurantId);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Order> orders = orderRepository
                .findByRestaurantIdOrderByCreatedAtDesc(restaurantId);

        Map<LocalDate, BigDecimal> dailySales = new TreeMap<>();
        Map<LocalDate, Long> dailyOrders = new TreeMap<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            dailySales.put(date, BigDecimal.ZERO);
            dailyOrders.put(date, 0L);
        }

        BigDecimal totalSales = BigDecimal.ZERO;
        long totalOrders = 0;
        long deliveredOrders = 0;

        for (Order order : orders) {
            if (order.getCreatedAt() == null) {
                continue;
            }

            LocalDate date = order.getCreatedAt().toLocalDate();

            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                continue;
            }

            dailyOrders.put(date, dailyOrders.get(date) + 1);
            totalOrders++;

            if ("DELIVERED".equals(order.getStatus())) {
                BigDecimal amount = order.getTotalAmount();

                totalSales = totalSales.add(amount);
                deliveredOrders++;

                dailySales.put(
                        date,
                        dailySales.get(date).add(amount)
                );
            }
        }

        BigDecimal averageOrderValue =
                deliveredOrders == 0
                        ? BigDecimal.ZERO
                        : totalSales.divide(
                        BigDecimal.valueOf(deliveredOrders),
                        2,
                        RoundingMode.HALF_UP
                );

        List<DailyPerformanceResponse> dailyPerformance =
                new ArrayList<>();

        for (LocalDate date : dailySales.keySet()) {
            dailyPerformance.add(
                    new DailyPerformanceResponse(
                            date,
                            dailySales.get(date),
                            dailyOrders.get(date)
                    )
            );
        }

        return new MerchantPerformanceResponse(
                days,
                totalSales,
                totalOrders,
                averageOrderValue,
                dailyPerformance
        );
    }


    @Transactional(readOnly = true)
    public List<TopSellingItemResponse> getTopSellingItems(
            Long merchantId, Long restaurantId) {

        verifyRestaurantOwnership(merchantId, restaurantId);

        List<OrderItem> items =
                orderItemRepository.findByOrder_Restaurant_Id(restaurantId);

        Map<String, TopSellingItemResponse> totals = new HashMap<>();

        for (OrderItem item : items) {

            if (!"DELIVERED".equals(item.getOrder().getStatus())) {
                continue;
            }

            Long menuItemId = item.getMenuItem() != null
                    ? item.getMenuItem().getId()
                    : null;

            String imageUrl = item.getMenuItem() != null
                    ? item.getMenuItem().getImageUrl()
                    : null;

            String key = menuItemId != null
                    ? "ID:" + menuItemId
                    : "NAME:" + item.getItemName();

            TopSellingItemResponse result = totals.computeIfAbsent(
                    key,
                    ignored -> new TopSellingItemResponse(
                            menuItemId,
                            item.getItemName(),
                            imageUrl,
                            0,
                            BigDecimal.ZERO
                    )
            );

            result.addSale(item.getQuantity(), item.getSubtotal());
        }

        return totals.values().stream()
                .sorted(
                        Comparator.comparingInt(
                                TopSellingItemResponse::getQuantitySold
                        ).reversed()
                )
                .toList();
    }


}