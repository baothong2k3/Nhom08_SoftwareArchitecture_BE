package com.bookstore.controllers;

import com.bookstore.entities.Cart;
import com.bookstore.services.CartService;
import com.bookstore.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public List<Cart> getCarts(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Bỏ "Bearer " để lấy token
            String username = jwtUtil.extractUsername(token); // Lấy username từ token

            // Lấy userId từ username (giả sử bạn có phương thức này trong CartService)
            Long userId = cartService.getUserIdByUsername(username);

            return cartService.getCartsByUser(userId);
        }

        throw new RuntimeException("Authorization token not found or invalid");
    }
}
