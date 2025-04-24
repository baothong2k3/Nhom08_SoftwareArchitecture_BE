package com.bookstore.services.impl;

import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;
import com.bookstore.repositories.OrderRepository;
import com.bookstore.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Order createOrder(Long userId, List<CartResponseDTO> cartItems) {
        // Tạo đơn hàng
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PLACED);
        order.setTotalPrice(cartItems.stream()
                .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(item.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        Order savedOrder = orderRepository.save(order);

        // Tạo danh sách OrderDetail
        List<OrderDetail> orderDetails = new ArrayList<>(); // Use a mutable list
        for (CartResponseDTO cartItem : cartItems) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .order(savedOrder)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .bookId(cartItem.getBookId())
                    .bookTitle(cartItem.getBookTitle())
                    .build();
            orderDetails.add(orderDetail);
        }

        // Lưu OrderDetail vào database
        savedOrder.setOrderDetails(orderDetails);
        orderRepository.save(savedOrder);

        // Cập nhật stock_quantity cho từng sách
        for (CartResponseDTO cartItem : cartItems) {
            String bookServiceUrl = "http://localhost:8003/api/books/" + cartItem.getBookId() + "/update-stock";
            restTemplate.patchForObject(bookServiceUrl, cartItem.getQuantity(), Void.class);
        }

        return savedOrder;
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findAllByStatusOrderByCreatedAtDesc(status);
    }
}