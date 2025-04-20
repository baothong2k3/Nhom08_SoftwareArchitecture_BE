package com.bookstore.dtos;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class BookDTO {
    private String title;
    private String author;
    private BigDecimal price;
    private int stockQuantity;
    private String category;
    private String description;
    private boolean status;
    private String imageUrl;
    private String publicId;
    private MultipartFile imageFile;
}