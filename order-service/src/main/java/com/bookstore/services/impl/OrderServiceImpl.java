package com.bookstore.services.impl;

import com.bookstore.dtos.CartRequestDTO;
import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.dtos.OrderRequestDTO;
import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;
import com.bookstore.repositories.OrderRepository;
import com.bookstore.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
    public String createOrder(Long userId, OrderRequestDTO orderRequestDTO,List<CartRequestDTO> cartRequestDTOList) {
        // 1. Kiểm tra co đủ sách trong kho không
        for (CartRequestDTO item : cartRequestDTOList) {
            String url = "http://localhost:8080/api/books/" + item.getBookId() + "/check-stock?requestedQuantity=" + item.getQuantity();
            Boolean isEnough;
            try {
                isEnough = restTemplate.getForObject(url, Boolean.class);
            } catch (Exception e) {
                return "Không thể kết nối đến dịch vụ kiểm tra kho sách (Book Service) cho sách ID " + item.getBookId();
            }
            if (!isEnough) {
                return "Không đủ sách trong kho cho sách ID " + item.getBookId() + ". Số lượng yêu cầu: " + item.getQuantity();
            }
        }
        // 2. Tạo đối tượng Order
        Order order = Order.builder()
                .userId(userId)
                .totalPrice(orderRequestDTO.getTotalPrice())
                .status(OrderStatus.PLACED)
                .userName(orderRequestDTO.getUserName())
                .phoneNumber(orderRequestDTO.getPhoneNumber())
                .email(orderRequestDTO.getEmail())
                .shippingAddress(orderRequestDTO.getShippingAddress())
                .paymentMethod(orderRequestDTO.getPaymentMethod())
                .build();
        // 3. Tạo danh sách OrderDetail từ cartRequestDTOList
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartRequestDTO item : cartRequestDTOList) {
            OrderDetail detail = OrderDetail.builder()
                    .bookId(item.getBookId())
                    .bookTitle(item.getBookTitle())
                    .price(item.getPrice())
                    .image(item.getImage())
                    .totalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .quantity(item.getQuantity())
                    .order(order)
                    .build();

            String bookServiceUrl = "http://localhost:8080/api/books/" + item.getBookId() + "/update-stock";
            try {
                restTemplate.patchForObject(bookServiceUrl, item.getQuantity(), Void.class);
            } catch (Exception e) {
                return "Không thể cập nhật số lượng tồn cho sách ID " + item.getBookId();
            }
            orderDetails.add(detail);
        }
        // 5. Gán danh sách OrderDetail cho Order
        order.setOrderDetails(orderDetails);
        // 6. Lưu Order (sẽ tự cascade lưu OrderDetail)
        try {
            orderRepository.save(order);
            return "Đơn hàng đã được tạo thành công";
        } catch (Exception e) {
            return "Đã xảy ra lỗi khi tạo đơn hàng: " + e.getMessage();
        }
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findAllByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<OrderDetail> getOrderDetailsByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        return order.getOrderDetails();
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        // Validate status transition
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }
        order.setStatus(newStatus);
        // Nếu hủy đơn hàng, xóa tất cả chi tiết đơn hàng
        if (newStatus == OrderStatus.CANCELED) {
            order.getOrderDetails().clear();
        }
        return orderRepository.save(order);
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        switch (currentStatus) {
            case PLACED:
                return newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELED;
            case CONFIRMED:
                return newStatus == OrderStatus.SHIPPING || newStatus == OrderStatus.CANCELED;
            case SHIPPING:
                return newStatus == OrderStatus.DELIVERED;
            case DELIVERED:
                return newStatus == OrderStatus.REJECTED;
            case CANCELED:
                return false; // No transitions allowed
            default:
                return false;
        }
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }


    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

}