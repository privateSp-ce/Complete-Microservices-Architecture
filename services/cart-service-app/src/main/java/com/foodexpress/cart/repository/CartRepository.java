package com.foodexpress.cart.repository;

import com.foodexpress.cart.entity.Cart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends CrudRepository<Cart, String> {

    // Redis repositories don't support complex querying out of the box unless using RediSearch.
    // We rely on Key-Value access mainly.

    Optional<Cart> findByUserId(String userId);

}
