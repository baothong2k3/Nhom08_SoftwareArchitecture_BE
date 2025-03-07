package com.bookstore.services;

import com.bookstore.entities.Cart;

import java.util.List;

public interface CartService {
    public Cart saveCart(Long bookId, Long userId);

    public List<Cart> getCartsByUser(Long userId);

    public Integer getCountCart(Long userId);

    public void updateQuantity(String sy, Long cartId);

    public void deleteBookInCart(Long cartId);

    public void updateQuantityBookInCart(Cart cart);
}
