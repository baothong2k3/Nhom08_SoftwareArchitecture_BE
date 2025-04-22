package com.bookstore.services;

import com.bookstore.dtos.CartResponseDTO;

import java.util.List;

public interface CartService {
    CartResponseDTO addBookToCart(Long userId, Long bookId);
    List<CartResponseDTO> getAllBooksInCart(Long userId);
    void removeBookFromCart(Long userId, Long bookId);
}