package com.foodexpress.restaurant.config;

import com.foodexpress.restaurant.entity.MenuItem;
import com.foodexpress.restaurant.entity.Restaurant;
import com.foodexpress.restaurant.entity.enums.CuisineType;
import com.foodexpress.restaurant.entity.enums.DietaryType;
import com.foodexpress.restaurant.entity.enums.SpiceLevel;
import com.foodexpress.restaurant.repository.MenuItemRepository;
import com.foodexpress.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataSeeder {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (restaurantRepository.count() > 0) {
                log.info("Restaurants already seeded, skipping.");
                return;
            }

            log.info("Seeding Restaurant Data...");

            // 1. Pizza Hut
            Restaurant pizzaHut = Restaurant.builder()
                    .name("Pizza Hut")
                    .description("Delicious Pizzas and Sides")
                    .ownerUserId(1L)
                    .cuisineTypes("ITALIAN,FAST_FOOD")
                    .city("Hyderabad")
                    .state("Telangana")
                    .pincode("500081")
                    .openingTime(LocalTime.of(10, 0))
                    .closingTime(LocalTime.of(23, 0))
                    .isActive(true)
                    .averageRating(BigDecimal.valueOf(4.5))
                    .totalRatings(120)
                    .minimumOrderAmount(BigDecimal.valueOf(200))
                    .deliveryFee(BigDecimal.valueOf(40))
                    .build();
            pizzaHut = restaurantRepository.save(pizzaHut);

            MenuItem pizza = MenuItem.builder()
                    .restaurantId(pizzaHut.getId())
                    .name("Margherita Pizza")
                    .description("Classic cheese pizza")
                    .price(BigDecimal.valueOf(250))
                    .isAvailable(true)
                    .dietaryType(DietaryType.VEGETARIAN)
                    .spiceLevel(SpiceLevel.MILD)
                    .build();
            menuItemRepository.save(pizza);
            pizzaHut.addMenuItem(pizza);
            restaurantRepository.save(pizzaHut);

            // 2. Spicy Wok
            Restaurant spicyWok = Restaurant.builder()
                    .name("Spicy Wok")
                    .description("Authentic Chinese Cuisine")
                    .ownerUserId(2L)
                    .cuisineTypes("CHINESE,THAI")
                    .city("Hyderabad")
                    .state("Telangana")
                    .pincode("500032")
                    .openingTime(LocalTime.of(11, 0))
                    .closingTime(LocalTime.of(22, 30))
                    .isActive(true)
                    .averageRating(BigDecimal.valueOf(4.2))
                    .totalRatings(85)
                    .minimumOrderAmount(BigDecimal.valueOf(300))
                    .deliveryFee(BigDecimal.valueOf(30))
                    .build();
            spicyWok = restaurantRepository.save(spicyWok);

            MenuItem noodles = MenuItem.builder()
                    .restaurantId(spicyWok.getId())
                    .name("Hakka Noodles")
                    .description("Stir fried noodles with veggies")
                    .price(BigDecimal.valueOf(180))
                    .isAvailable(true)
                    .dietaryType(DietaryType.VEGETARIAN)
                    .spiceLevel(SpiceLevel.MEDIUM)
                    .build();
            menuItemRepository.save(noodles);
            spicyWok.addMenuItem(noodles);
            restaurantRepository.save(spicyWok);

            log.info("Seeding Completed. Created {} restaurants.", restaurantRepository.count());
        };
    }
}
