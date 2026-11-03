package com.foodie.customer.shopping;

import com.foodie.model.MenuItem;
import com.foodie.model.MenuItemSize;
import com.foodie.model.Restaurant;
import com.foodie.repository.MenuItemRepository;
import com.foodie.repository.RestaurantRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Customer-facing menu: categories, items, descriptions, prices,
 * sizes and availability for approved restaurants.
 */
@RestController
public class MenuController {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public MenuController(
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    // GET /api/menu-items            -> every item of every approved restaurant
    // GET /api/menu-items?restaurantId=3 -> one restaurant only
    // Unavailable items are included with available=false so the UI can grey them out.
    @GetMapping("/api/menu-items")
    @Transactional(readOnly = true)
    public List<MenuItemResponse> listMenuItems(
            @RequestParam(required = false) Long restaurantId
    ) {
        List<MenuItem> items = restaurantId == null
                ? menuItemRepository.findAll()
                : menuItemRepository.findByRestaurantId(restaurantId);

        return items.stream()
                .filter(item -> RestaurantController
                        .isOpenForCustomers(item.getRestaurant()))
                .sorted(Comparator.comparing(MenuItem::getId))
                .map(MenuController::toResponse)
                .toList();
    }

    // GET /api/restaurants/{restaurantId}/menu -> items grouped by category
    @GetMapping("/api/restaurants/{restaurantId}/menu")
    @Transactional(readOnly = true)
    public MenuResponse getRestaurantMenu(@PathVariable Long restaurantId) {
        Restaurant restaurant = RestaurantController
                .findOpenRestaurant(restaurantRepository, restaurantId);

        Map<String, List<MenuItemResponse>> itemsByCategory =
                menuItemRepository.findByRestaurantId(restaurantId).stream()
                        .sorted(Comparator.comparing(MenuItem::getName,
                                String.CASE_INSENSITIVE_ORDER))
                        .map(MenuController::toResponse)
                        .collect(Collectors.groupingBy(
                                MenuItemResponse::category,
                                () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER),
                                Collectors.toList()
                        ));

        List<MenuCategoryResponse> categories = itemsByCategory.entrySet()
                .stream()
                .map(entry -> new MenuCategoryResponse(
                        entry.getKey(), entry.getValue()
                ))
                .toList();

        return new MenuResponse(
                restaurant.getId(),
                restaurant.getRestaurantName(),
                categories
        );
    }

    private static MenuItemResponse toResponse(MenuItem item) {
        // The React item page expects sizes as { "Large": { selected, price } }.
        Map<String, SizeOption> sizes = new LinkedHashMap<>();
        item.getSizes().stream()
                .sorted(Comparator.comparing(MenuItemSize::getPrice))
                .forEach(size -> sizes.put(
                        size.getSizeName(),
                        new SizeOption(true, size.getPrice())
                ));

        Restaurant restaurant = item.getRestaurant();

        return new MenuItemResponse(
                item.getId(),
                restaurant.getId(),
                restaurant.getRestaurantName(),
                restaurant.getRestaurantName(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                RestaurantController.categoryName(item.getCategory()),
                item.getImageUrl(),
                item.getImageUrl(),
                item.isAvailable(),
                !sizes.isEmpty(),
                sizes
        );
    }

    public record SizeOption(boolean selected, BigDecimal price) {
    }

    public record MenuItemResponse(
            Long id,
            Long restaurantId,
            String restaurantName,
            // Same as restaurantName; the React dish cards and cart read "restaurant".
            String restaurant,
            String name,
            String description,
            BigDecimal price,
            String category,
            String imageUrl,
            String image,
            boolean available,
            boolean sizesEnabled,
            Map<String, SizeOption> sizes
    ) {
    }

    public record MenuCategoryResponse(
            String name,
            List<MenuItemResponse> items
    ) {
    }

    public record MenuResponse(
            Long restaurantId,
            String restaurantName,
            List<MenuCategoryResponse> categories
    ) {
    }
}
