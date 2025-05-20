package com.bookstore.dtos;

import com.bookstore.entities.OrderDetail;
import com.bookstore.entities.PaymentMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class OrderMessage {
    private Long id;
    private String email;
    private String phoneNumber;
    private String shippingAddress;
    private String paymentMethod;
    private String createdAt;
    private BigDecimal totalPrice;
}
