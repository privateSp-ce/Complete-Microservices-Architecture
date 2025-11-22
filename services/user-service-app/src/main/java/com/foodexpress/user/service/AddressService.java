package com.foodexpress.user.service;

import com.foodexpress.user.dto.request.AddressCreateRequest;
import com.foodexpress.user.dto.request.AddressUpdateRequest;
import com.foodexpress.user.dto.response.AddressResponse;
import com.foodexpress.user.entity.Address;
import com.foodexpress.user.entity.User;
import com.foodexpress.user.exception.BadRequestException;
import com.foodexpress.user.exception.ResourceNotFoundException;
import com.foodexpress.user.repository.AddressRepository;
import com.foodexpress.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for address management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /**
     * Get current authenticated user's email
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("No authenticated user found");
        }
        return authentication.getName();
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Get all addresses for current user
     */
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses() {
        User user = getCurrentUser();
        log.info("Fetching all addresses for user: {}", user.getEmail());

        List<Address> addresses = addressRepository.findByUserId(user.getId());
        return addresses.stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get address by ID for current user
     */
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId) {
        User user = getCurrentUser();

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        log.info("Fetching address {} for user: {}", addressId, user.getEmail());
        return AddressResponse.fromEntity(address);
    }

    /**
     * Get default address for current user
     */
    @Transactional(readOnly = true)
    public AddressResponse getDefaultAddress() {
        User user = getCurrentUser();

        Address address = addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Default address not found"));

        log.info("Fetching default address for user: {}", user.getEmail());
        return AddressResponse.fromEntity(address);
    }

    /**
     * Create new address for current user
     */
    @Transactional
    public AddressResponse createAddress(AddressCreateRequest request) {
        User user = getCurrentUser();
        log.info("Creating new address for user: {}", user.getEmail());

        // If this is set as default, unset all other defaults
        if (request.getIsDefault()) {
            addressRepository.unsetDefaultForUser(user.getId());
        } else {
            // If this is the first address, make it default
            long addressCount = addressRepository.countByUserId(user.getId());
            if (addressCount == 0) {
                request.setIsDefault(true);
            }
        }

        // Create address
        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .fullAddress(request.getFullAddress())
                .landmark(request.getLandmark())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isDefault(request.getIsDefault())
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully with ID: {} for user: {}", savedAddress.getId(), user.getEmail());

        return AddressResponse.fromEntity(savedAddress);
    }

    /**
     * Update address for current user
     */
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressUpdateRequest request) {
        User user = getCurrentUser();
        log.info("Updating address {} for user: {}", addressId, user.getEmail());

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Update fields if provided
        if (request.getLabel() != null) {
            address.setLabel(request.getLabel());
        }
        if (request.getFullAddress() != null) {
            address.setFullAddress(request.getFullAddress());
        }
        if (request.getLandmark() != null) {
            address.setLandmark(request.getLandmark());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getPincode() != null) {
            address.setPincode(request.getPincode());
        }
        if (request.getLatitude() != null) {
            address.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            address.setLongitude(request.getLongitude());
        }

        Address updatedAddress = addressRepository.save(address);
        log.info("Address {} updated successfully for user: {}", addressId, user.getEmail());

        return AddressResponse.fromEntity(updatedAddress);
    }

    /**
     * Set address as default
     */
    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        User user = getCurrentUser();
        log.info("Setting address {} as default for user: {}", addressId, user.getEmail());

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Unset all other defaults
        addressRepository.unsetDefaultForUser(user.getId());

        // Set this as default
        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);

        log.info("Address {} set as default for user: {}", addressId, user.getEmail());
        return AddressResponse.fromEntity(updatedAddress);
    }

    /**
     * Delete address for current user
     */
    @Transactional
    public void deleteAddress(Long addressId) {
        User user = getCurrentUser();
        log.info("Deleting address {} for user: {}", addressId, user.getEmail());

        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        // If deleted address was default, set another address as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserId(user.getId());
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
                log.info("Set new default address {} for user: {}", newDefault.getId(), user.getEmail());
            }
        }

        log.info("Address {} deleted successfully for user: {}", addressId, user.getEmail());
    }

    /**
     * Get address count for current user
     */
    public long getAddressCount() {
        User user = getCurrentUser();
        return addressRepository.countByUserId(user.getId());
    }
}