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
}