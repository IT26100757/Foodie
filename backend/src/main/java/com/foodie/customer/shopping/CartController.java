package com.foodie.customer.shopping;

import com.foodie.model.MenuItem;
import com.foodie.model.MenuItemSize;
import com.foodie.model.Restaurant;
import com.foodie.repository.MenuItemRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Shopping cart pricing. The cart itself lives in React state (add, update
 * quantity, remove); the backend is the source of truth for prices and totals.
 * OrderController reuses priceCart(...) so checkout is validated the same way.
 */
@RestController
@RequestMapping("/api/customer/cart")
public class CartController {

    static final BigDecimal DELIVERY_FEE = new BigDecimal("150.00");

    private final MenuItemRepository menuItemRepository;

    public CartController(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    // POST /api/customer/cart/quote
    // Body: { "items": [ { "menuItemId": 1, "sizeName": "Large", "quantity": 2 } ] }
    @PostMapping("/quote")
    @Transactional(readOnly = true)
    public CartQuote quote(@Valid @RequestBody CartRequest request) {
        return priceCart(request.items());
    }

    /**
     * Validates every cart line against the database and calculates totals.
     * Throws 400 Bad Request with a readable message when something is wrong.
     */
    public CartQuote priceCart(List<CartItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw badRequest("Your cart is empty");
        }

        Restaurant restaurant = null;
        List<CartLine> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;

        for (CartItemRequest requested : items) {
            MenuItem menuItem = menuItemRepository
                    .findById(requested.menuItemId())
                    .orElseThrow(() -> badRequest(
                            "Menu item " + requested.menuItemId() + " no longer exists"
                    ));

            if (!RestaurantController.isOpenForCustomers(menuItem.getRestaurant())) {
                throw badRequest("This restaurant is not accepting orders");
            }

            if (!menuItem.isAvailable()) {
                throw badRequest(menuItem.getName() + " is currently unavailable");
            }

            // One order belongs to one restaurant (orders.restaurant_id).
            if (restaurant == null) {
                restaurant = menuItem.getRestaurant();
            } else if (!restaurant.getId().equals(menuItem.getRestaurant().getId())) {
                throw badRequest("All items in one order must be from the same restaurant");
            }

            String sizeName = resolveSizeName(menuItem, requested.sizeName());
            BigDecimal unitPrice = money(unitPrice(menuItem, sizeName));
            BigDecimal lineTotal = money(
                    unitPrice.multiply(BigDecimal.valueOf(requested.quantity()))
            );

            lines.add(new CartLine(
                    menuItem.getId(),
                    menuItem.getName(),
                    sizeName,
                    requested.quantity(),
                    unitPrice,
                    lineTotal
            ));

            subtotal = subtotal.add(lineTotal);
            itemCount += requested.quantity();
        }

        BigDecimal total = money(subtotal.add(DELIVERY_FEE));

        return new CartQuote(
                restaurant.getId(),
                restaurant.getRestaurantName(),
                List.copyOf(lines),
                itemCount,
                money(subtotal),
                DELIVERY_FEE,
                total
        );
    }

    private static String resolveSizeName(MenuItem menuItem, String requestedSize) {
        boolean hasSizes = !menuItem.getSizes().isEmpty();
        boolean sizeRequested = requestedSize != null && !requestedSize.isBlank();

        if (!hasSizes) {
            if (sizeRequested) {
                throw badRequest(menuItem.getName() + " does not have sizes");
            }
            return null;
        }

        if (!sizeRequested) {
            throw badRequest("Choose a size for " + menuItem.getName());
        }

        return menuItem.getSizes().stream()
                .map(MenuItemSize::getSizeName)
                .filter(name -> name.equalsIgnoreCase(requestedSize.trim()))
                .findFirst()
                .orElseThrow(() -> badRequest(
                        "Size " + requestedSize + " is not available for "
                                + menuItem.getName()
                ));
    }

    private static BigDecimal unitPrice(MenuItem menuItem, String sizeName) {
        if (sizeName == null) {
            return menuItem.getPrice();
        }

        return menuItem.getSizes().stream()
                .filter(size -> size.getSizeName().equals(sizeName))
                .map(MenuItemSize::getPrice)
                .findFirst()
                .orElse(menuItem.getPrice());
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public record CartItemRequest(
            @NotNull Long menuItemId,
            String sizeName,
            @NotNull @Min(1) @Max(50) Integer quantity
    ) {
    }

    public record CartRequest(
            @NotEmpty @Size(max = 50) List<@Valid @NotNull CartItemRequest> items
    ) {
    }

    public record CartLine(
            Long menuItemId,
            String name,
            String sizeName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }

    public record CartQuote(
            Long restaurantId,
            String restaurantName,
            List<CartLine> items,
            int itemCount,
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal total
    ) {
    }
}
