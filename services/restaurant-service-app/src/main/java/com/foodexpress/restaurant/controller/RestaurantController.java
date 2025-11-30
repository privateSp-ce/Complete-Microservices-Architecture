package com.foodexpress.restaurant.controller;

import com.foodexpress.restaurant.dto.ReviewDto;
import com.foodexpress.restaurant.entity.Restaurant;
import com.foodexpress.restaurant.entity.Review;
import com.foodexpress.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    @PostMapping("/{id}/reviews")
    public ResponseEntity<Void> addReview(@PathVariable String id, @RequestBody ReviewDto reviewDto) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow();
        Review review = new Review();
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        // For simplicity, hardcoding user. In real app, extract from SecurityContext.
        review.setUserId("current-user-id");
        review.setUserName("Current User");

        restaurant.getReviews().add(review);
        restaurantRepository.save(restaurant);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Restaurant>> search(@RequestParam String query) {
        return ResponseEntity.ok(restaurantRepository.searchByName(query));
    }
}
