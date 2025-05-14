package com.bookstore.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PlaceOrderRequestDTO {
    private List<CartRequestDTO> cartRequest;
    private OrderRequestDTO orderRequest;
}
