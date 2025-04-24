package com.bookstore.services;

import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;

import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, List<CartResponseDTO> cartItems);
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getOrdersByStatus(OrderStatus status);
    List<OrderDetail> getOrderDetailsByOrderId(Long orderId);
}
