package com.spring_cloud.user_service_app.repository;

import com.spring_cloud.user_service_app.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find all addresses for a user
     */
    List<Address> findByUserId(Long userId);

    /**
     * Find address by id and user id
     */
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    /**
     * Find default address for a user
     */
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Check if address exists for user
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Count addresses for a user
     */
    long countByUserId(Long userId);

    /**
     * Unset default for all addresses of a user (for setting new default)
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void unsetDefaultForUser(@Param("userId") Long userId);

    /**
     * Delete all addresses for a user
     */
    void deleteByUserId(Long userId);
}