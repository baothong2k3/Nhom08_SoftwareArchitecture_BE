package com.bookstore.services;

import com.bookstore.entities.Cart;
import java.util.List;

public interface CartService {
<<<<<<< HEAD
    Cart saveCart(Long bookId, Long userId, int stockQuantity);  // từ dev

    List<Cart> getCartsByUser(Long userId);

    Integer getCountCart(Long userId);

    void updateQuantity(String action, Long userId, Long bookId, int stockQuantity); // từ dev

    void deleteBookInCart(Long userId, Long bookId); // từ dev

    void updateQuantityBookInCart(Cart cart); // từ nguyen

    Long getUserIdByUsername(String username); // từ nguyen
=======
    List<Cart> getCartsByUser(Long userId);
    Cart saveCart(Long userId, Long bookId, int quantity);
    Cart updateQuantity(String username, Long cartId, Long bookId, int quantity);
    void deleteBookInCart(Long cartId, Long bookId);
    Long getUserIdByUsername(String username);
>>>>>>> 70eb395 (create docker)
}
