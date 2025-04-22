package com.bookstore.repositories;

import com.bookstore.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndBookId(Long userId, Long bookId);
    List<Cart> findByUserId(Long userId);
}