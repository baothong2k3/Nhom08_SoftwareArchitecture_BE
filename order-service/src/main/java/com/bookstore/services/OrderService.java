package com.bookstore.services;

import com.bookstore.entities.Order;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);
    Order getOrderById(Long id);
    List<Order> getAllOrders();
    List<Order> getOrdersByUserId(Long userId);
    void deleteOrder(Long id);
    Order updateOrderStatus(Long id, String status);
}
