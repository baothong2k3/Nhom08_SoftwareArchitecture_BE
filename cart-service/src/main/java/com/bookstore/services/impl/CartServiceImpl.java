package com.bookstore.services.impl;

import com.bookstore.entities.Cart;
import com.bookstore.repositories.CartRepository;
import com.bookstore.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;

    @Override
    public List<Cart> getCartsByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    public Cart saveCart(Long bookId, Long userId, int stockQuantity) {
        Optional<Cart> existingCart = cartRepository.findByUserIdAndBookId(userId, bookId);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + stockQuantity);
            return cartRepository.save(cart);
        }
        Cart newCart = Cart.builder()
                .userId(userId)
                .bookId(bookId)
                .quantity(stockQuantity)
                .build();
        return cartRepository.save(newCart);
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

    @Override
    public Long getUserIdByUsername(String username) {
        // TODO: Implement after UserClient is available
        return null;
    }
}
