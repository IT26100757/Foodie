package com.foodie.customer.shopping;

import com.foodie.customer.account.CustomerAccount;
import com.foodie.customer.account.CustomerAccountRepository;
import com.foodie.customer.account.CustomerAddress;
import com.foodie.customer.account.CustomerAddressRepository;
import com.foodie.customer.account.CustomerPrincipal;
import com.foodie.customer.shopping.CartController.CartItemRequest;
import com.foodie.customer.shopping.CartController.CartQuote;
import com.foodie.model.Order;
import com.foodie.model.OrderItem;
import com.foodie.repository.MenuItemRepository;
import com.foodie.repository.OrderItemRepository;
import com.foodie.repository.OrderRepository;
import com.foodie.repository.RestaurantRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Checkout, order history, order details/status and cancellation for the
 * logged-in customer. Uses the shared Order/OrderItem model so new orders
 * appear in the Merchant Portal automatically.
 *
 * NOTE: the shared orders table has no customer_id column yet, so orders are
 * linked to the customer through customer_phone (unique per customer account).
 * Switch to customer_id once the team leader adds that column.
 */
@RestController
@RequestMapping("/api/customer/orders")
public class OrderController {

    // Merchant Portal statuses: NEW -> PREPARING -> READY -> ON_DELIVERY -> DELIVERED
    private static final String STATUS_NEW = "NEW";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private static final Map<String, String> PAYMENT_METHODS = Map.of(
            "card", "CARD",
            "cash", "CASH_ON_DELIVERY"
    );

    private static final int MAX_REMEMBERED_CHECKOUTS = 10_000;

    private final CartController cartController;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final EntityManager entityManager;

    // customerId:checkoutId -> orderId, used to ignore double-clicks on "Place order".
    private final Map<String, Long> completedCheckouts = new ConcurrentHashMap<>();

    public OrderController(
            CartController cartController,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository,
            CustomerAccountRepository customerAccountRepository,
            CustomerAddressRepository customerAddressRepository,
            EntityManager entityManager
    ) {
        this.cartController = cartController;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.customerAccountRepository = customerAccountRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.entityManager = entityManager;
    }

    // POST /api/customer/orders  (checkout)
    @PostMapping
    @Transactional
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @Valid @RequestBody CheckoutRequest request
    ) {
        Long customerId = requireCustomerId(principal);

        // Locks this customer's row until the transaction ends, so two
        // simultaneous checkouts from the same customer run one at a time.
        CustomerAccount customer = customerAccountRepository
                .findByIdForUpdate(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        String checkoutKey = customerId + ":" + request.checkoutId();
        Optional<Order> alreadyPlaced = Optional
                .ofNullable(completedCheckouts.get(checkoutKey))
                .flatMap(orderRepository::findById);
        if (alreadyPlaced.isPresent()) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(toResponse(alreadyPlaced.get()));
        }

        // Never trust prices sent by the browser: re-price from the database.
        CartQuote quote = cartController.priceCart(request.items());

        Order order = new Order();
        order.setRestaurant(restaurantRepository.getReferenceById(quote.restaurantId()));
        order.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        order.setCustomerPhone(customer.getPhone());
        order.setDeliveryAddress(resolveDeliveryAddress(customerId, request.addressId()));
        order.setOrderType("DELIVERY");
        order.setPaymentMethod(resolvePaymentMethod(request.paymentMethod()));
        order.setStatus(STATUS_NEW);
        order.setTotalAmount(quote.total());

        Order saved = orderRepository.saveAndFlush(order);

        List<OrderItem> orderItems = quote.items().stream()
                .map(line -> {
                    OrderItem item = new OrderItem();
                    item.setOrder(saved);
                    item.setMenuItem(menuItemRepository.getReferenceById(line.menuItemId()));
                    item.setItemName(line.sizeName() == null
                            ? line.name()
                            : line.name() + " (" + line.sizeName() + ")");
                    item.setQuantity(line.quantity());
                    item.setUnitPrice(line.unitPrice());
                    item.setSubtotal(line.lineTotal());
                    return item;
                })
                .toList();
        orderItemRepository.saveAll(orderItems);
        orderItemRepository.flush();

        // created_at is filled in by the database, so read it back.
        entityManager.refresh(saved);

        if (completedCheckouts.size() >= MAX_REMEMBERED_CHECKOUTS) {
            completedCheckouts.clear();
        }
        completedCheckouts.put(checkoutKey, saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .cacheControl(CacheControl.noStore())
                .body(toResponse(saved, orderItems));
    }

    // GET /api/customer/orders  (order history, newest first)
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> listOrders(
            @AuthenticationPrincipal CustomerPrincipal principal
    ) {
        CustomerAccount customer = requireCustomer(principal);

        Order probe = new Order();
        probe.setCustomerPhone(customer.getPhone());
        // Order() has default values; ignore them so only the phone is matched.
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths(
                "orderType", "status", "additionalCharge", "additionalChargeStatus"
        );

        List<OrderResponse> orders = orderRepository
                .findAll(Example.of(probe, matcher),
                        Sort.by(Sort.Direction.DESC, "createdAt", "id"))
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(orders);
    }

    // GET /api/customer/orders/{orderId}  (details + status for tracking)
    @GetMapping("/{orderId}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @PathVariable Long orderId
    ) {
        Order order = findOwnOrder(requireCustomer(principal), orderId);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(toResponse(order));
    }

    // PATCH /api/customer/orders/{orderId}/cancel
    @PatchMapping("/{orderId}/cancel")
    @Transactional
    public ResponseEntity<OrderResponse> cancelOrder(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @PathVariable Long orderId
    ) {
        Order order = findOwnOrder(requireCustomer(principal), orderId);

        if (!canCustomerCancel(order)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This order can no longer be cancelled (status: "
                            + order.getStatus() + ")"
            );
        }

        order.setStatus(STATUS_CANCELLED);
        orderRepository.save(order);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(toResponse(order));
    }

    /**
     * Business rule: a customer may cancel only before the restaurant starts
     * preparing the food. After that only the merchant can cancel.
     */
    private static boolean canCustomerCancel(Order order) {
        return STATUS_NEW.equals(order.getStatus());
    }

    private Order findOwnOrder(CustomerAccount customer, Long orderId) {
        // Someone else's order returns 404, so order ids cannot be probed.
        return orderRepository.findById(orderId)
                .filter(order -> customer.getPhone().equals(order.getCustomerPhone()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found"
                ));
    }

    private String resolveDeliveryAddress(Long customerId, Long addressId) {
        Optional<CustomerAddress> address = addressId == null
                // No address chosen: use the default (sorted first), if any.
                ? customerAddressRepository
                        .findAllByCustomer_IdOrderByDefaultAddressDescIdAsc(customerId)
                        .stream()
                        .findFirst()
                // Chosen address must belong to this customer.
                : customerAddressRepository.findByIdAndCustomer_Id(addressId, customerId);

        return address
                .map(OrderController::formatAddress)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Choose a delivery address"
                ));
    }

    private static String formatAddress(CustomerAddress address) {
        return Stream.of(
                        address.getAddress1(),
                        address.getAddress2(),
                        address.getAddress3(),
                        address.getCity(),
                        address.getPostalCode()
                )
                .filter(part -> part != null && !part.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    private static String resolvePaymentMethod(String paymentMethod) {
        String method = PAYMENT_METHODS.get(paymentMethod.trim().toLowerCase(Locale.ROOT));
        if (method == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment method must be card or cash"
            );
        }
        return method;
    }

    private CustomerAccount requireCustomer(CustomerPrincipal principal) {
        return customerAccountRepository.findById(requireCustomerId(principal))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private static Long requireCustomerId(CustomerPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return principal.getCustomerId();
    }

    private OrderResponse toResponse(Order order) {
        return toResponse(order, orderItemRepository.findByOrderId(order.getId()));
    }

    private static OrderResponse toResponse(Order order, List<OrderItem> items) {
        List<OrderLine> lines = items.stream()
                .map(item -> new OrderLine(
                        item.getItemName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        BigDecimal subtotal = lines.stream()
                .map(OrderLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                order.getId(),
                order.getRestaurant().getId(),
                order.getRestaurant().getRestaurantName(),
                order.getStatus(),
                canCustomerCancel(order),
                order.getDeliveryAddress(),
                order.getPaymentMethod(),
                subtotal,
                order.getTotalAmount().subtract(subtotal),
                order.getTotalAmount(),
                order.getCreatedAt(),
                lines
        );
    }

    public record CheckoutRequest(
            // Generated once per checkout page in React (crypto.randomUUID()).
            @NotBlank @Size(max = 64) String checkoutId,
            Long addressId,
            @NotBlank String paymentMethod,
            @NotEmpty @Size(max = 50) List<@Valid @NotNull CartItemRequest> items
    ) {
    }

    public record OrderLine(
            String itemName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }

    public record OrderResponse(
            Long id,
            Long restaurantId,
            String restaurantName,
            String status,
            boolean cancellable,
            String deliveryAddress,
            String paymentMethod,
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal totalAmount,
            LocalDateTime createdAt,
            List<OrderLine> items
    ) {
    }
}
