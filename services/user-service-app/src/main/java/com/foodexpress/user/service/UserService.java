package com.foodexpress.user.service;

import com.foodexpress.user.dto.AddressDto;
import com.foodexpress.user.dto.UserProfileDto;
import com.foodexpress.user.dto.request.PasswordChangeRequest;
import com.foodexpress.user.dto.request.UserUpdateRequest;
import com.foodexpress.user.dto.response.UserResponse;
import com.foodexpress.user.entity.Address;
import com.foodexpress.user.entity.User;
import com.foodexpress.user.exception.BadRequestException;
import com.foodexpress.user.exception.DuplicateResourceException;
import com.foodexpress.user.exception.ResourceNotFoundException;
import com.foodexpress.user.repository.AddressRepository;
import com.foodexpress.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user profile management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

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
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Get current user profile
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfileResponse() {
        User user = getCurrentUser();
        log.info("Fetching profile for user: {}", user.getEmail());
        return UserResponse.fromEntity(user);
    }

    /**
     * Get current user profile with addresses
     * Since I removed OneToMany addresses from User, I fetch them manually if needed, or just return basic user.
     * The original code called findByIdWithAddresses which used JOIN FETCH.
     * Since I decoupled them, I can't use that exactly same way on the entity easily without re-adding the relation.
     * For now, I'll return standard UserResponse. If client needs addresses, they call /addresses.
     * Or I can manually fetch addresses and add to a DTO if UserResponse supports it.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfileWithAddresses() {
        User user = getCurrentUser();
        log.info("Fetching profile with addresses for user: {}", user.getEmail());
        // Addresses are now fetched via separate endpoint /api/users/addresses
        // So this method just returns the user info.
        return UserResponse.fromEntity(user);
    }

    /**
     * Get user by ID (for internal use)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        log.info("Fetching user by ID: {}", userId);
        return UserResponse.fromEntity(user);
    }

    /**
     * Update current user profile
     */
    @Transactional
    public UserResponse updateCurrentUserProfile(UserUpdateRequest request) {
        User user = getCurrentUser();
        log.info("Updating profile for user: {}", user.getEmail());

        // Update first name if provided
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        // Update last name if provided
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        // Update phone if provided and different
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            // Check if new phone already exists
            if (userRepository.existsByPhone(request.getPhone())) {
                log.warn("Phone update failed: Phone already exists - {}", request.getPhone());
                throw new DuplicateResourceException("User", "phone", request.getPhone());
            }
            user.setPhone(request.getPhone());
            user.setPhoneVerified(false); // Reset phone verification
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", updatedUser.getEmail());

        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Change password for current user
     */
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        User user = getCurrentUser();
        log.info("Password change request for user: {}", user.getEmail());

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed: Current password incorrect for user: {}", user.getEmail());
            throw new BadRequestException("Current password is incorrect");
        }

        // Verify new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("Password change failed: New passwords don't match for user: {}", user.getEmail());
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Verify new password is different from current
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            log.warn("Password change failed: New password same as current for user: {}", user.getEmail());
            throw new BadRequestException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    /**
     * Soft delete current user account
     */
    @Transactional
    public void deleteCurrentUser() {
        User user = getCurrentUser();
        log.info("Deactivating account for user: {}", user.getEmail());

        user.setIsActive(false);
        userRepository.save(user);

        log.info("Account deactivated successfully for user: {}", user.getEmail());
    }

    /**
     * Check if user exists by ID (for internal/inter-service use)
     */
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    // --- New Methods ---

    public void addAddress(AddressDto dto) {
        User user = getCurrentUser();
        Address address = new Address();
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());
        address.setType(dto.getType());
        address.setUserId(user.getId());
        addressRepository.save(address);
    }

    public List<AddressDto> getUserAddresses() {
        User user = getCurrentUser();
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public UserProfileDto getUserProfile() {
        User user = getCurrentUser();
        UserProfileDto dto = new UserProfileDto();
        dto.setUsername(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setPreferences(user.getPreferences());
        return dto;
    }

    @Transactional
    public void updateUserProfile(UserProfileDto dto) {
        User user = getCurrentUser();
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }
        if (dto.getPreferences() != null) {
            user.setPreferences(dto.getPreferences());
        }
        userRepository.save(user);
    }

    private AddressDto mapToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        dto.setType(address.getType());
        return dto;
    }
}
