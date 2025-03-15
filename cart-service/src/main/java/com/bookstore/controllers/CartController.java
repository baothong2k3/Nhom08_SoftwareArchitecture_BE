package com.bookstore.controllers;

import com.bookstore.entities.Cart;
import com.bookstore.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public Cart addBookToCart(@RequestParam Long userId, @RequestParam Long bookId, @RequestParam int stockQuantity) {
        return cartService.saveCart(bookId, userId, stockQuantity);
    }

    @GetMapping("/user/{userId}")
    public List<Cart> getUserCart(@PathVariable Long userId) {
        return cartService.getCartsByUser(userId);
    }

    @PutMapping("/update")
    public void updateCart(@RequestParam String action, @RequestParam Long userId, @RequestParam Long bookId, @RequestParam int stockQuantity) {
        cartService.updateQuantity(action, userId, bookId, stockQuantity);
    }

    @DeleteMapping("/remove")
    public void removeBookFromCart(@RequestParam Long userId, @RequestParam Long bookId) {
        cartService.deleteBookInCart(userId, bookId);
    }
}
