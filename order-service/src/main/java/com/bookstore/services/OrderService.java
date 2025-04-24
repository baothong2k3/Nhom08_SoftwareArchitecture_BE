package com.bookstore.services;

import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.entities.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, List<CartResponseDTO> cartItems);
    Order save(Order order);
}
