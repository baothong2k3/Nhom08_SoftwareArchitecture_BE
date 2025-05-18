package com.bookstore.services.impl;

import com.bookstore.dtos.CartRequestDTO;
import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.dtos.OrderMessage;
import com.bookstore.dtos.OrderRequestDTO;
import com.bookstore.entities.Order;
import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.OrderStatus;
import com.bookstore.repositories.OrderRepository;
import com.bookstore.services.OrderService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Value("${app.rabbit.queue-name}")
    private String queueName;
    @Value("${app.rabbit.exchange-name}")
    private String exchangeName;
    @Value("${app.rabbit.routing-key}")
    private String routingKey;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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
            Order or = orderRepository.save(order);
            if(orderRequestDTO.getEmail() != null && !orderRequestDTO.getEmail().isEmpty()){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                String createdAtString = order.getCreatedAt().format(formatter);
                OrderMessage orderMessage = OrderMessage.builder()
                        .email(orderRequestDTO.getEmail())
                        .phoneNumber(orderRequestDTO.getPhoneNumber())
                        .shippingAddress(orderRequestDTO.getShippingAddress())
                        .paymentMethod(orderRequestDTO.getPaymentMethod().toString())
                        .createdAt(createdAtString)
                        .id(or.getId())
                        .totalPrice(orderRequestDTO.getTotalPrice())
                        .build();
                rabbitTemplate.convertAndSend(exchangeName, routingKey, orderMessage);
            }
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


    @Override
    public List<Map<String, Object>> getTopSellingBooks(LocalDate startDate, LocalDate endDate) {
        List<Order> orders;

        if (startDate != null && endDate != null) {
            orders = orderRepository.findAllByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        } else {
            orders = orderRepository.findAllByStatus(OrderStatus.DELIVERED);
        }

        Map<Long, Map<String, Object>> bookSales = new HashMap<>();
        for (Order order : orders) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Long bookId = detail.getBookId();
                bookSales.putIfAbsent(bookId, new HashMap<>());
                Map<String, Object> bookStat = bookSales.get(bookId);

                bookStat.put("bookId", bookId);
                bookStat.put("bookTitle", detail.getBookTitle());
                bookStat.put("quantitySold", (int) bookStat.getOrDefault("quantitySold", 0) + detail.getQuantity());
                bookStat.put("totalPrice", ((BigDecimal) bookStat.getOrDefault("totalPrice", BigDecimal.ZERO))
                        .add(detail.getTotalPrice()));
            }
        }

        return bookSales.values().stream()
                .sorted((b1, b2) -> ((Integer) b2.get("quantitySold")).compareTo((Integer) b1.get("quantitySold")))
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getMonthlyRevenue(int year) {
        List<Order> orders = orderRepository.findAllByStatusAndYear(OrderStatus.DELIVERED, year);

        // Initialize revenue for all 12 months with 0
        Map<Integer, BigDecimal> revenueByMonth = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            revenueByMonth.put(i, BigDecimal.ZERO);
        }

        // Calculate revenue for months with orders
        for (Order order : orders) {
            int month = order.getCreatedAt().getMonthValue();
            revenueByMonth.put(month, revenueByMonth.get(month).add(order.getTotalPrice()));
        }

        // Convert to list of maps
        return revenueByMonth.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> monthRevenue = new HashMap<>();
                    monthRevenue.put("month", entry.getKey());
                    monthRevenue.put("revenue", entry.getValue());
                    return monthRevenue;
                })
                .sorted((m1, m2) -> Integer.compare((int) m1.get("month"), (int) m2.get("month")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getYearlyRevenue(int startYear, int endYear) {
        List<Order> orders = orderRepository.findAllByStatusAndYearRange(OrderStatus.DELIVERED, startYear, endYear);

        // Initialize revenue for all years in the range with 0
        Map<Integer, BigDecimal> revenueByYear = new HashMap<>();
        for (int year = startYear; year <= endYear; year++) {
            revenueByYear.put(year, BigDecimal.ZERO);
        }

        // Calculate revenue for years with orders
        for (Order order : orders) {
            int year = order.getCreatedAt().getYear();
            revenueByYear.put(year, revenueByYear.get(year).add(order.getTotalPrice()));
        }

        // Convert to list of maps
        return revenueByYear.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> yearRevenue = new HashMap<>();
                    yearRevenue.put("year", entry.getKey());
                    yearRevenue.put("revenue", entry.getValue());
                    return yearRevenue;
                })
                .sorted((y1, y2) -> Integer.compare((int) y1.get("year"), (int) y2.get("year")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getTopCustomers(LocalDate startDate, LocalDate endDate) {
        List<Order> orders;

        if (startDate != null && endDate != null) {
            orders = orderRepository.findAllByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        } else {
            orders = orderRepository.findAllByStatus(OrderStatus.DELIVERED);
        }

        Map<Long, Map<String, Object>> customerStats = new HashMap<>();
        for (Order order : orders) {
            Long userId = order.getUserId();
            customerStats.putIfAbsent(userId, new HashMap<>());
            Map<String, Object> customerStat = customerStats.get(userId);

            customerStat.put("userId", userId);
            customerStat.put("userName", order.getUserName());
            customerStat.put("totalSpent", ((BigDecimal) customerStat.getOrDefault("totalSpent", BigDecimal.ZERO))
                    .add(order.getTotalPrice()));
            customerStat.put("orderCount", (int) customerStat.getOrDefault("orderCount", 0) + 1);
        }

        return customerStats.values().stream()
                .sorted((c1, c2) -> ((BigDecimal) c2.get("totalSpent")).compareTo((BigDecimal) c1.get("totalSpent")))
                .limit(10)
                .collect(Collectors.toList());
    }


    @Override
    public Page<Order> getPagedOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

}