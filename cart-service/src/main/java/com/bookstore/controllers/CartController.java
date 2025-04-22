package com.bookstore.controllers;

import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addBookToCart(
            @RequestParam Long userId,
            @RequestParam Long bookId) {
        CartResponseDTO response = cartService.addBookToCart(userId, bookId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CartResponseDTO>> getAllBooksInCart(@RequestParam Long userId) {
        List<CartResponseDTO> response = cartService.getAllBooksInCart(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<List<CartResponseDTO>> removeBookFromCart(
            @RequestParam Long userId,
            @RequestParam Long bookId) {
        cartService.removeBookFromCart(userId, bookId);
        List<CartResponseDTO> updatedCart = cartService.getAllBooksInCart(userId);
        return ResponseEntity.ok(updatedCart);
    }
}