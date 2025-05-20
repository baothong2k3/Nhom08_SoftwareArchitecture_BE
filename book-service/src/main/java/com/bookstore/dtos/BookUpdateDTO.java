package com.bookstore.dtos;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class BookUpdateDTO {
    private Long id;
    @NotBlank(message = "Tiêu đề sách không được để trống")
    @Size(max = 255, message = "Tiêu đề sách không được vượt quá 255 ký tự")
    private String title;
    @NotBlank(message = "Tên tác giả không được để trống")
    @Size(max = 255, message = "Tên tác giả không được vượt quá 255 ký tự")
    private String author;
    @NotNull(message = "Giá sách là bắt buộc")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    private int stockQuantity;
    @NotBlank(message = "Danh mục không được để trống")
    private String category;
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;
    private boolean status;
    private String imageUrl;
    private String publicId;
    private MultipartFile imageFile;
    @Min(value = 0, message = "Phần trăm giảm giá phải từ 0 đến 100")
    @Max(value = 100, message = "Phần trăm giảm giá phải từ 0 đến 100")
    private Integer discountPercent;
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá sau giảm không được âm")
    private BigDecimal discountedPrice;
}
