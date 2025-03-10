package com.bookstore.services.impl;

import com.bookstore.entities.Cart;
import com.bookstore.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartService cartService;
    @Override
    public Cart saveCart(Long bookId, Long userId) {
        return null;
    }

    @Override
    public List<Cart> getCartsByUser(Long userId) {
        return List.of();
    }

    @Override
    public Integer getCountCart(Long userId) {
        return 0;
    }

    @Override
    public void updateQuantity(String sy, Long cartId) {

    }

    @Override
    public void deleteBookInCart(Long cartId) {

    }

    @Override
    public void updateQuantityBookInCart(Cart cart) {

    }

}
