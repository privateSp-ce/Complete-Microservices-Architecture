package com.spring_cloud.user_service_app.service;

import com.spring_cloud.user_service_app.dto.request.PasswordChangeRequest;
import com.spring_cloud.user_service_app.dto.request.UserUpdateRequest;
import com.spring_cloud.user_service_app.dto.response.UserResponse;
import com.spring_cloud.user_service_app.entity.User;
import com.spring_cloud.user_service_app.exception.BadRequestException;
import com.spring_cloud.user_service_app.exception.DuplicateResourceException;
import com.spring_cloud.user_service_app.exception.ResourceNotFoundException;
import com.spring_cloud.user_service_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user profile management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
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
    private User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Get current user profile
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        log.info("Fetching profile for user: {}", user.getEmail());
        return UserResponse.fromEntity(user);
    }

    /**
     * Get current user profile with addresses
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfileWithAddresses() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByIdWithAddresses(getCurrentUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        log.info("Fetching profile with addresses for user: {}", user.getEmail());
        return UserResponse.fromEntityWithAddresses(user);
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
}