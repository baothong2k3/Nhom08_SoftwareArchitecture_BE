package com.bookstore.dtos;


import com.bookstore.entities.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class OrderRequestDTO {
    @NotNull(message = "Total price is required")
    private BigDecimal totalPrice;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "User name is required")
    private String userName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String email;
}
