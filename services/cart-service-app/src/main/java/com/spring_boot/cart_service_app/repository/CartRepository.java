package com.spring_boot.cart_service_app.repository;

import com.spring_boot.cart_service_app.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndIsActiveTrue(Long userId);

    Optional<Cart> findByIdAndUserId(Long id, Long userId);

    List<Cart> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime expiryTime);

    @Query("SELECT c FROM Cart c WHERE c.userId = :userId AND c.isActive = true")
    Optional<Cart> findActiveCartByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndIsActiveTrue(Long userId);

    boolean existsByUserIdAndIsActiveTrue(Long userId);
}