package com.bookstore.services;

import com.bookstore.dtos.CartRequestDTO;
import com.bookstore.dtos.OrderRequestDTO;
import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;

import java.util.List;

public interface OrderService {
    String createOrder(Long userId, OrderRequestDTO orderRequestDTO, List<CartRequestDTO> cartRequestDTOList);
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getOrdersByStatus(OrderStatus status);
    List<OrderDetail> getOrderDetailsByOrderId(Long orderId);
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
    List<Order> getAllOrders();
}
