package com.bookstore.services;

import com.bookstore.dtos.CartRequestDTO;
import com.bookstore.dtos.OrderRequestDTO;
import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderService {
    String createOrder(Long userId, OrderRequestDTO orderRequestDTO, List<CartRequestDTO> cartRequestDTOList);
    List<Order> getOrdersByUserId(Long userId);
    List<Order> getOrdersByStatus(OrderStatus status);
    List<OrderDetail> getOrderDetailsByOrderId(Long orderId);
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    List<Map<String, Object>> getTopSellingBooks(LocalDate startDate, LocalDate endDate);
    List<Map<String, Object>> getMonthlyRevenue(int year);
    List<Map<String, Object>> getYearlyRevenue(int startYear, int endYear);
}
