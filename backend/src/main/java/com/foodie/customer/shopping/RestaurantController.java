package com.foodie.customer.shopping;

import com.foodie.model.MenuItem;
import com.foodie.model.Merchant;
import com.foodie.model.Restaurant;
import com.foodie.repository.MenuItemRepository;
import com.foodie.repository.RestaurantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Customer-facing restaurant browsing: list, search, filter and details.
 * Reads the shared Restaurant/MenuItem model owned by the Merchant Portal.
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantController(
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    // GET /api/restaurants?search=pizza&category=Burgers
    @GetMapping
    @Transactional(readOnly = true)
    public List<RestaurantResponse> listRestaurants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    ) {
        Map<Long, List<String>> categoriesByRestaurant =
                availableCategoriesByRestaurant();

        String searchText = normalize(search);
        String categoryText = normalize(category);

        return restaurantRepository.findAll().stream()
                .filter(RestaurantController::isOpenForCustomers)
                .map(restaurant -> toResponse(
                        restaurant,
                        categoriesByRestaurant.getOrDefault(
                                restaurant.getId(), List.of()
                        )
                ))
                .filter(restaurant -> searchText.isEmpty()
                        || restaurant.name().toLowerCase(Locale.ROOT)
                                .contains(searchText)
                        || restaurant.address().toLowerCase(Locale.ROOT)
                                .contains(searchText)
                        || restaurant.type().toLowerCase(Locale.ROOT)
                                .contains(searchText))
                .filter(restaurant -> categoryText.isEmpty()
                        || restaurant.categories().stream()
                                .anyMatch(name -> name.equalsIgnoreCase(categoryText)))
                .sorted(Comparator.comparing(RestaurantResponse::name,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    // GET /api/restaurants/{restaurantId}
    @GetMapping("/{restaurantId}")
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurant(@PathVariable Long restaurantId) {
        Restaurant restaurant = findOpenRestaurant(restaurantRepository, restaurantId);

        List<String> categories = menuItemRepository
                .findByRestaurantId(restaurantId).stream()
                .filter(MenuItem::isAvailable)
                .map(item -> categoryName(item.getCategory()))
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        return toResponse(restaurant, categories);
    }

    /**
     * Only restaurants whose merchant has been approved by an admin are
     * visible to customers. Reused by MenuController and CartController.
     */
    static boolean isOpenForCustomers(Restaurant restaurant) {
        Merchant merchant = restaurant.getMerchant();
        return merchant != null
                && merchant.getVerificationStatus()
                        == Merchant.VerificationStatus.APPROVED;
    }

    static Restaurant findOpenRestaurant(
            RestaurantRepository restaurantRepository,
            Long restaurantId
    ) {
        return restaurantRepository.findById(restaurantId)
                .filter(RestaurantController::isOpenForCustomers)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Restaurant not found"
                ));
    }

    static String categoryName(String category) {
        return category == null || category.isBlank()
                ? "Other"
                : category.trim();
    }

    private Map<Long, List<String>> availableCategoriesByRestaurant() {
        return menuItemRepository.findAll().stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.groupingBy(
                        item -> item.getRestaurant().getId(),
                        Collectors.collectingAndThen(
                                Collectors.mapping(
                                        item -> categoryName(item.getCategory()),
                                        Collectors.toList()
                                ),
                                names -> names.stream()
                                        .distinct()
                                        .sorted(String.CASE_INSENSITIVE_ORDER)
                                        .toList()
                        )
                ));
    }

    private static RestaurantResponse toResponse(
            Restaurant restaurant,
            List<String> categories
    ) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getRestaurantName(),
                Objects.requireNonNullElse(restaurant.getRestaurantAddress(), ""),
                restaurant.getRestaurantPhone(),
                String.join(" · ", categories),
                categories,
                CartController.DELIVERY_FEE
        );
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    /** JSON sent to the Customer Portal. "type" feeds the existing card subtitle. */
    public record RestaurantResponse(
            Long id,
            String name,
            String address,
            String phone,
            String type,
            List<String> categories,
            // Shown on the restaurant card as "Rs. 150"; same fee CartController charges.
            BigDecimal deliveryFee
    ) {
    }
}
