package com.bookstore.dtos;


import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;


@Getter
@Setter
public class CartRequestDTO {
    private Long cartId;
    private String image;
    private Long bookId;
    private int quantity;
    private BigDecimal price;
    private String bookTitle;
}
