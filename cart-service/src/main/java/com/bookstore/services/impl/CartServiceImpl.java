package com.bookstore.cartservice.services.impl;

import com.bookstore.cartservice.clients.UserClient;
import com.bookstore.cartservice.dtos.UserDTO;
import com.bookstore.cartservice.entities.Cart;
import com.bookstore.cartservice.repositories.CartRepository;
import com.bookstore.cartservice.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public Cart saveCart(Long bookId, Long userId, int stockQuantity) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndBookId(userId, bookId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + stockQuantity);
            return cartRepository.save(cart);
        }
        Cart newCart = new Cart(null, userId, bookId, stockQuantity);
        return cartRepository.save(newCart);
    }

    @Override
    public List<Cart> getCartsByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public Integer getCountCart(Long userId) {
        return cartRepository.findByUserId(userId).size();
    }

    @Override
    public void updateQuantity(String action, Long userId, Long bookId, int stockQuantity) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndBookId(userId, bookId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            if ("increase".equals(action)) {
                cart.setQuantity(cart.getQuantity() + stockQuantity);
            } else if ("decrease".equals(action) && cart.getQuantity() > stockQuantity) {
                cart.setQuantity(cart.getQuantity() - stockQuantity);
            } else {
                cartRepository.delete(cart);
                return;
            }
            cartRepository.save(cart);
        }
    }

    @Override
    public void deleteBookInCart(Long userId, Long bookId) {
        cartRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    @Override
    public void updateQuantityBookInCart(Cart cart) {
        cartRepository.save(cart);
    }
}
