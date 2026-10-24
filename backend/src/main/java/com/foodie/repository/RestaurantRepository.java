
package com.foodie.repository;

import com.foodie.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository
        extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByMerchantId(Long merchantId);

}
