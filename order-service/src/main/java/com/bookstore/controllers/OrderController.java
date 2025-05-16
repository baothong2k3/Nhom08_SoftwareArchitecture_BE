package com.bookstore.controllers;

import com.bookstore.dtos.CartRequestDTO;
import com.bookstore.dtos.PlaceOrderRequestDTO;
import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;
import com.bookstore.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String auth,
            @RequestHeader("UserId") Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("message", "Không tìm thấy đơn hàng");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (!order.getUserId().equals(userId)) {
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("message", "Không tìm thấy đơn hàng");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (order.getStatus() == OrderStatus.PLACED || order.getStatus() == OrderStatus.CONFIRMED) {
                Order canceledOrder = orderService.updateOrderStatus(orderId, OrderStatus.CANCELED);
                // Gọi tăng lại stock cho từng sản phẩm trong order
                for (OrderDetail detail : canceledOrder.getOrderDetails()) {
                    try {
                        Long bookId = detail.getBookId();
                        int quantity = detail.getQuantity();
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set("Authorization", auth);

                        String url = "http://localhost:8080/api/books/" + bookId + "/increase-stock";
                        HttpEntity<Integer> request = new HttpEntity<>(quantity, headers);
                        ResponseEntity<Void> bookResponse = new RestTemplate().exchange(
                                url,
                                HttpMethod.PATCH,
                                request,
                                Void.class
                        );
                        System.out.println("Đã tăng lại số lượng cho sách ID " + bookId + ", status: " + bookResponse.getStatusCode());
                    } catch (Exception e) {
                        System.err.println("Lỗi khi gọi tăng stock: " + e.getMessage());
                    }
                }
                response.put("status", HttpStatus.OK.value());
                response.put("message", "Đơn hàng đã được huỷ thành công và số lượng sách đã được hoàn lại");
                response.put("order", canceledOrder);
                return ResponseEntity.ok(response);
            }
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Không thể huỷ đơn hàng ở trạng thái hiện tại");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi huỷ đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



}
