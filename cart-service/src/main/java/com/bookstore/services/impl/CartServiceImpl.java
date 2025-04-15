package com.bookstore.cartservice.services.impl;

import com.bookstore.cartservice.clients.UserClient;
import com.bookstore.cartservice.dtos.UserDTO;
import com.bookstore.cartservice.entities.Cart;
import com.bookstore.cartservice.repositories.CartRepository;
import com.bookstore.cartservice.services.CartService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserClient userClient;

    public CartServiceImpl(CartRepository cartRepository, UserClient userClient) {
        this.cartRepository = cartRepository;
        this.userClient = userClient;
    }

    @Override
    public Long getUserIdByUsername(String token) {
        UserDTO userDTO = userClient.getCurrentUser(token);
        return userDTO != null ? userDTO.getId() : null;
    }

    @Override
    public Cart saveCart(Long bookId, Long userId) {
        // Triển khai thêm nếu cần
        return null;
    }

    @Override
    public List<Cart> getCartsByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public Integer getCountCart(Long userId) {
        return cartRepository.countByUserId(userId);
    }

    @Override
    public void updateQuantity(String sy, Long cartId) {
        // Triển khai logic
    }

    @Override
    public void deleteBookInCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }

    @Override
    public void updateQuantityBookInCart(Cart cart) {
        cartRepository.save(cart);
    }
}
