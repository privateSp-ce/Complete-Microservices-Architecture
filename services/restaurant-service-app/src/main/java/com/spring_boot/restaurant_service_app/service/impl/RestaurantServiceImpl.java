package com.spring_boot.restaurant_service_app.service.impl;

import com.spring_boot.restaurant_service_app.dto.common.AddressDTO;
import com.spring_boot.restaurant_service_app.dto.common.GeoLocationDTO;
import com.spring_boot.restaurant_service_app.dto.common.PageResponse;
import com.spring_boot.restaurant_service_app.dto.request.RestaurantRegistrationRequest;
import com.spring_boot.restaurant_service_app.dto.request.RestaurantSearchRequest;
import com.spring_boot.restaurant_service_app.dto.request.RestaurantUpdateRequest;
import com.spring_boot.restaurant_service_app.dto.response.RestaurantResponse;
import com.spring_boot.restaurant_service_app.dto.response.RestaurantSummaryResponse;
import com.spring_boot.restaurant_service_app.entity.Restaurant;
import com.spring_boot.restaurant_service_app.exception.DuplicateResourceException;
import com.spring_boot.restaurant_service_app.exception.ResourceNotFoundException;
import com.spring_boot.restaurant_service_app.repository.RestaurantRepository;
import com.spring_boot.restaurant_service_app.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Override
    public RestaurantResponse registerRestaurant(RestaurantRegistrationRequest request) {
        log.info("Registering new restaurant: {}", request.getName());

        // Check if owner already has a restaurant
        if (restaurantRepository.existsByOwnerUserId(request.getOwnerUserId())) {
            throw new DuplicateResourceException("Owner already has a registered restaurant");
        }

        // Build full address
        AddressDTO address = request.getAddress();
        String fullAddress = String.format("%s, %s, %s, %s - %s",
                address.getStreet() != null ? address.getStreet() : "",
                address.getLandmark() != null ? address.getLandmark() : "",
                address.getCity(),
                address.getState(),
                address.getPincode());

        // Create restaurant entity
        Restaurant restaurant = Restaurant.builder()
                .ownerUserId(request.getOwnerUserId())
                .name(request.getName())
                .description(request.getDescription())
                .fullAddress(fullAddress)
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .latitude(BigDecimal.valueOf(request.getLocation().getLatitude()))
                .longitude(BigDecimal.valueOf(request.getLocation().getLongitude()))
                .phone(request.getPhoneNumber())
                .email(request.getEmail())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .minimumOrderAmount(request.getMinimumOrderAmount())
                .deliveryFee(request.getDeliveryFee())
                .logoUrl(request.getImageUrl())
                .isActive(true)
                .isVerified(false)
                .isAcceptingOrders(false) // Need verification first
                .build();

        // Set cuisine types
        restaurant.setCuisineTypesList(request.getCuisineTypes());

        // Save restaurant
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant registered successfully with ID: {}", savedRestaurant.getId());

        return mapToRestaurantResponse(savedRestaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        log.info("Fetching restaurant with ID: {}", id);
        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
        return mapToRestaurantResponse(restaurant);
    }

    @Override
    public RestaurantResponse updateRestaurant(Long id, RestaurantUpdateRequest request) {
        log.info("Updating restaurant with ID: {}", id);

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            restaurant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            restaurant.setDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            AddressDTO address = request.getAddress();
            String fullAddress = String.format("%s, %s, %s, %s - %s",
                    address.getStreet() != null ? address.getStreet() : "",
                    address.getLandmark() != null ? address.getLandmark() : "",
                    address.getCity(),
                    address.getState(),
                    address.getPincode());
            restaurant.setFullAddress(fullAddress);
            restaurant.setCity(address.getCity());
            restaurant.setState(address.getState());
            restaurant.setPincode(address.getPincode());
        }
        if (request.getLocation() != null) {
            restaurant.setLatitude(BigDecimal.valueOf(request.getLocation().getLatitude()));
            restaurant.setLongitude(BigDecimal.valueOf(request.getLocation().getLongitude()));
        }
        if (request.getCuisineTypes() != null) {
            restaurant.setCuisineTypesList(request.getCuisineTypes());
        }
        if (request.getPhoneNumber() != null) {
            restaurant.setPhone(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            restaurant.setEmail(request.getEmail());
        }
        if (request.getOpeningTime() != null) {
            restaurant.setOpeningTime(request.getOpeningTime());
        }
        if (request.getClosingTime() != null) {
            restaurant.setClosingTime(request.getClosingTime());
        }
        if (request.getMinimumOrderAmount() != null) {
            restaurant.setMinimumOrderAmount(request.getMinimumOrderAmount());
        }
        if (request.getDeliveryFee() != null) {
            restaurant.setDeliveryFee(request.getDeliveryFee());
        }
        if (request.getImageUrl() != null) {
            restaurant.setLogoUrl(request.getImageUrl());
        }
        if (request.getIsAcceptingOrders() != null) {
            restaurant.setIsAcceptingOrders(request.getIsAcceptingOrders());
        }
        if (request.getIsActive() != null) {
            restaurant.setIsActive(request.getIsActive());
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant updated successfully with ID: {}", updatedRestaurant.getId());

        return mapToRestaurantResponse(updatedRestaurant);
    }

    @Override
    public void deleteRestaurant(Long id) {
        log.info("Deleting restaurant with ID: {}", id);

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));

        restaurant.setIsActive(false);
        restaurant.setIsAcceptingOrders(false);
        restaurantRepository.save(restaurant);

        log.info("Restaurant soft deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getRestaurantsByOwner(Long ownerUserId) {
        log.info("Fetching restaurants for owner: {}", ownerUserId);
        List<Restaurant> restaurants = restaurantRepository.findByOwnerUserIdAndIsActiveTrue(ownerUserId);
        return restaurants.stream()
                .map(this::mapToRestaurantResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RestaurantSummaryResponse> searchRestaurants(RestaurantSearchRequest request) {
        log.info("Searching restaurants with filters");

        // Create pageable
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 20;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "averageRating";
        String direction = request.getSortDirection() != null ? request.getSortDirection() : "desc";

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // For now, return all active restaurants (implement filtering later)
        List<Restaurant> restaurants = restaurantRepository.findByIsVerifiedTrueAndIsActiveTrue();

        // Apply filters manually (this is simplified - in production use Specifications)
        List<Restaurant> filtered = restaurants.stream()
                .filter(r -> request.getName() == null || r.getName().toLowerCase().contains(request.getName().toLowerCase()))
                .filter(r -> request.getCity() == null || r.getCity().equalsIgnoreCase(request.getCity()))
                .filter(r -> request.getAcceptingOrders() == null || r.getIsAcceptingOrders().equals(request.getAcceptingOrders()))
                .collect(Collectors.toList());

        List<RestaurantSummaryResponse> content = filtered.stream()
                .map(this::mapToRestaurantSummaryResponse)
                .collect(Collectors.toList());

        return PageResponse.<RestaurantSummaryResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(content.size())
                .totalPages((int) Math.ceil((double) content.size() / size))
                .first(page == 0)
                .last(page >= (int) Math.ceil((double) content.size() / size) - 1)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantSummaryResponse> getRestaurantsByCity(String city) {
        log.info("Fetching restaurants in city: {}", city);
        List<Restaurant> restaurants = restaurantRepository.findByCityAndIsVerifiedTrueAndIsActiveTrue(city);
        return restaurants.stream()
                .map(this::mapToRestaurantSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantSummaryResponse> getNearbyRestaurants(Double latitude, Double longitude, Double radiusKm) {
        log.info("Fetching nearby restaurants at {}, {} within {} km", latitude, longitude, radiusKm);

        // Simple bounding box calculation
        // 1 degree latitude ≈ 111 km
        // 1 degree longitude ≈ 111 km * cos(latitude)
        double latRange = radiusKm / 111.0;
        double lngRange = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        double minLat = latitude - latRange;
        double maxLat = latitude + latRange;
        double minLng = longitude - lngRange;
        double maxLng = longitude + lngRange;

        List<Restaurant> restaurants = restaurantRepository.findNearby(minLat, maxLat, minLng, maxLng);

        return restaurants.stream()
                .map(r -> {
                    RestaurantSummaryResponse response = mapToRestaurantSummaryResponse(r);
                    // Calculate distance
                    double distance = calculateDistance(latitude, longitude,
                            r.getLatitude().doubleValue(), r.getLongitude().doubleValue());
                    response.setDistanceInKm(distance);
                    return response;
                })
                .sorted((r1, r2) -> Double.compare(r1.getDistanceInKm(), r2.getDistanceInKm()))
                .collect(Collectors.toList());
    }

    @Override
    public RestaurantResponse verifyRestaurant(Long id) {
        log.info("Verifying restaurant with ID: {}", id);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));

        restaurant.setIsVerified(true);
        restaurant.setIsAcceptingOrders(true);
        Restaurant verifiedRestaurant = restaurantRepository.save(restaurant);

        log.info("Restaurant verified successfully with ID: {}", id);
        return mapToRestaurantResponse(verifiedRestaurant);
    }

    @Override
    public RestaurantResponse toggleAcceptingOrders(Long id) {
        log.info("Toggling accepting orders for restaurant ID: {}", id);

        Restaurant restaurant = restaurantRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));

        restaurant.setIsAcceptingOrders(!restaurant.getIsAcceptingOrders());
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        log.info("Restaurant accepting orders status toggled to: {}", updatedRestaurant.getIsAcceptingOrders());
        return mapToRestaurantResponse(updatedRestaurant);
    }

    // Helper methods
    private RestaurantResponse mapToRestaurantResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .ownerUserId(restaurant.getOwnerUserId().toString())
                .phoneNumber(restaurant.getPhone())
                .email(restaurant.getEmail())
                .address(AddressDTO.builder()
                        .street(restaurant.getFullAddress())
                        .city(restaurant.getCity())
                        .state(restaurant.getState())
                        .pincode(restaurant.getPincode())
                        .build())
                .location(GeoLocationDTO.builder()
                        .latitude(restaurant.getLatitude().doubleValue())
                        .longitude(restaurant.getLongitude().doubleValue())
                        .build())
                .cuisineTypes(restaurant.getCuisineTypesList())
                .openingTime(restaurant.getOpeningTime())
                .closingTime(restaurant.getClosingTime())
                .rating(restaurant.getAverageRating())
                .totalRatings(restaurant.getTotalRatings())
                .isActive(restaurant.getIsActive())
                .isVerified(restaurant.getIsVerified())
                .acceptingOrders(restaurant.getIsAcceptingOrders())
                .imageUrl(restaurant.getLogoUrl())
                .build();
    }

    private RestaurantSummaryResponse mapToRestaurantSummaryResponse(Restaurant restaurant) {
        return RestaurantSummaryResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .cuisineTypes(restaurant.getCuisineTypesList())
                .rating(restaurant.getAverageRating())
                .totalRatings(restaurant.getTotalRatings())
                .acceptingOrders(restaurant.getIsAcceptingOrders())
                .imageUrl(restaurant.getLogoUrl())
                .city(restaurant.getCity())
                .build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}