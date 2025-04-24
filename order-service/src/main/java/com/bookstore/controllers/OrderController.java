package com.bookstore.controllers;

import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;
import com.bookstore.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/place")
    public ResponseEntity<Order> placeOrder(@RequestParam Long userId) {
        // Lấy danh sách sách trong giỏ hàng từ cart-service
        String cartServiceUrl = "http://localhost:8004/api/cart/all?userId=" + userId;
        ResponseEntity<List<CartResponseDTO>> response = restTemplate.exchange(
                cartServiceUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CartResponseDTO>>() {}
        );
        List<CartResponseDTO> cartItems = response.getBody();

        if (cartItems == null || cartItems.isEmpty()) {
            return ResponseEntity.badRequest().build(); // Giỏ hàng trống
        }

        // Tạo đơn hàng
        Order order = orderService.createOrder(userId, cartItems);

        // Xóa giỏ hàng của user trong cart-service
        String clearCartUrl = "http://localhost:8004/api/cart/clear?userId=" + userId;
        restTemplate.delete(clearCartUrl);

        return ResponseEntity.ok(order);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> getOrdersByUser(@RequestParam Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status")
    public ResponseEntity<List<Order>> getOrdersByStatus(@RequestParam OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
    @GetMapping("/{orderId}/details")
    public ResponseEntity<List<OrderDetail>> getOrderDetailsByOrderId(@PathVariable Long orderId) {
        List<OrderDetail> orderDetails = orderService.getOrderDetailsByOrderId(orderId);
        return ResponseEntity.ok(orderDetails);
    }

    @PatchMapping("/{orderId}/update-status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }
}
