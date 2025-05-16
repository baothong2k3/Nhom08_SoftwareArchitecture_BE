package com.bookstore.controllers;

import com.bookstore.dtos.CartRequestDTO;
import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.dtos.OrderRequestDTO;
import com.bookstore.dtos.PlaceOrderRequestDTO;
import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;
import com.bookstore.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(
            @Valid @RequestBody PlaceOrderRequestDTO placeOrderRequestDTO,
            @RequestHeader("Authorization") String auth,
            @RequestHeader("UserId") Long userId) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        if(placeOrderRequestDTO.getOrderRequest() == null) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Không có thông tin đơn hàng");
            return ResponseEntity.badRequest().body(response);
        }
        // Kiểm tra nếu cartRequest là null hoặc rỗng
        if (placeOrderRequestDTO.getCartRequest() == null || placeOrderRequestDTO.getCartRequest().isEmpty()) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Giỏ hàng không có sản phẩm nào");
            return ResponseEntity.badRequest().body(response);
        }
        // Tạo đơn hàng
        String orderResponse = orderService.createOrder(userId, placeOrderRequestDTO.getOrderRequest(), placeOrderRequestDTO.getCartRequest());
        // Kiểm tra kết quả trả về từ service
        if (orderResponse.equals("Đơn hàng đã được tạo thành công")) {
            List<Long> cartIds = new ArrayList<>();
            for (CartRequestDTO cart : placeOrderRequestDTO.getCartRequest()) {
                if (cart.getCartId() != null) {
                    cartIds.add(cart.getCartId());
                }
            }
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", auth);
                headers.set("UserId", userId.toString());
                String clearCartUrl = "http://localhost:8080/api/cart/remove-multiple";
                HttpEntity<List<Long>> clearEntity = new HttpEntity<>(cartIds, headers);
                ResponseEntity<Boolean> cartClearResponse = restTemplate.exchange(
                        clearCartUrl,
                        HttpMethod.DELETE,
                        clearEntity,
                        Boolean.class
                );
                if (!Boolean.TRUE.equals(cartClearResponse.getBody())) {
                    response.put("status", HttpStatus.BAD_REQUEST.value());
                    response.put("message", "Không thể xóa giỏ hàng sau khi đặt hàng");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
                response.put("status", HttpStatus.OK.value());
                response.put("message", orderResponse);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                System.err.println("Lỗi khi gọi cart-service để xóa giỏ hàng: " + e.getMessage());
            }
        } else {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", orderResponse);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }



    @GetMapping("/user")
    public ResponseEntity<List<Order>> getOrdersByUser(@RequestHeader("UserId") Long userId) {
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

    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }



}
