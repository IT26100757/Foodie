
package com.foodie.controller;

import com.foodie.dto.OrderItemResponse;
import com.foodie.dto.OrderResponse;
import com.foodie.dto.MerchantDashboardResponse;
import com.foodie.dto.MerchantPerformanceResponse;
import com.foodie.dto.TopSellingItemResponse;
import com.foodie.model.Merchant;
import com.foodie.repository.MerchantRepository;
import com.foodie.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/merchant/restaurants/{restaurantId}/orders")
public class OrderController {

    private final OrderService orderService;
    private final MerchantRepository merchantRepository;

    public OrderController(
            OrderService orderService,
            MerchantRepository merchantRepository) {
        this.orderService = orderService;
        this.merchantRepository = merchantRepository;
    }

    @GetMapping
    public List<OrderResponse> getOrders(
            @PathVariable Long restaurantId,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        return orderService.getOrders(merchantId, restaurantId)
                .stream()
                .map(OrderResponse::new)
                .toList();
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(
            @PathVariable Long restaurantId,
            @PathVariable Long orderId,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        return new OrderResponse(
                orderService.getOrder(
                        merchantId, restaurantId, orderId));
    }

    @GetMapping("/{orderId}/items")
    public List<OrderItemResponse> getOrderItems(
            @PathVariable Long restaurantId,
            @PathVariable Long orderId,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        return orderService.getOrderItems(
                        merchantId, restaurantId, orderId)
                .stream()
                .map(OrderItemResponse::new)
                .toList();
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(
            @PathVariable Long restaurantId,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        return new OrderResponse(
                orderService.updateOrderStatus(
                        merchantId,
                        restaurantId,
                        orderId,
                        body.get("status")));
    }

    @GetMapping("/count")
    public Map<String, Long> countOrders(
            @PathVariable Long restaurantId,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        return Map.of("totalOrders",
                orderService.countOrders(merchantId, restaurantId));
    }

    private Long getMerchantId(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getAuthorities().stream()
                        .noneMatch(a ->
                                "ROLE_MERCHANT".equals(a.getAuthority()))) {
            throw new org.springframework.web.server
                    .ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Merchant access required");
        }

        String email = authentication.getName();

        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Merchant not found"));

        return merchant.getId();
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(
            NoSuchElementException ex) {
        return Map.of("message", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(
            IllegalArgumentException ex) {
        return Map.of("message", ex.getMessage());
    }


    @GetMapping("/dashboard")
    public MerchantDashboardResponse getDashboard(
            @PathVariable Long restaurantId,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        return orderService.getDashboardStats(
                merchantId, restaurantId);
    }


    @GetMapping("/performance")
    public MerchantPerformanceResponse getPerformance(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "7") int days,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        return orderService.getBusinessPerformance(
                merchantId, restaurantId, days);
    }

    @GetMapping("/top-selling")
    public List<TopSellingItemResponse> getTopSellingItems(
            @PathVariable Long restaurantId,
            Authentication authentication) {

        Long merchantId = getMerchantId(authentication);

        return orderService.getTopSellingItems(
                merchantId, restaurantId);
    }

}
