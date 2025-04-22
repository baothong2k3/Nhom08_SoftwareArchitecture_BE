package com.bookstore.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private Long cartId;
    private Long userId;
    private Long bookId;
    private int quantity;
    private String bookTitle;
    private String bookAuthor;
    private int stockQuantity;
    private String bookCategory;
    private String bookDescription;
    private boolean bookStatus;
    private String bookImageUrl;
}