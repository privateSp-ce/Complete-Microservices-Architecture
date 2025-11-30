package com.foodexpress.user.repository;

import com.foodexpress.user.entity.User;
import com.foodexpress.user.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone
     */
    Optional<User> findByPhone(String phone);

    /**
     * Find active user by email
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);

    /**
     * Find user by email and role
     */
    Optional<User> findByEmailAndRole(String email, UserRole role);

    /**
     * Find user by id and isActive true
     */
    Optional<User> findByIdAndIsActiveTrue(Long id);

    // Removed @Query with JOIN FETCH addresses because the relationship was removed/refactored.
    // Address management is now handled via AddressRepository using userId.
}