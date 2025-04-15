package com.bookstore.services;

import com.bookstore.entities.Cart;
import java.util.List;

public interface CartService {
    Cart saveCart(Long bookId, Long userId);

    List<Cart> getCartsByUser(Long userId);

    Integer getCountCart(Long userId);

    void updateQuantity(String sy, Long cartId);

    void deleteBookInCart(Long cartId);

    void updateQuantityBookInCart(Cart cart);

    Long getUserIdByUsername(String username); // 👈 CHỈ khai báo
}
