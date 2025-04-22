package com.bookstore.services.impl;

import com.bookstore.dtos.BookDTO;
import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.entities.Cart;
import com.bookstore.repositories.CartRepository;
import com.bookstore.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public CartResponseDTO addBookToCart(Long userId, Long bookId) {
        // Gọi Book Service để lấy thông tin book
        String bookServiceUrl = "http://localhost:8003/api/books/" + bookId;
        BookDTO bookDTO = restTemplate.getForObject(bookServiceUrl, BookDTO.class);

        if (bookDTO == null || !bookDTO.isStatus()) {
            throw new IllegalArgumentException("Book not found or unavailable");
        }

        // Kiểm tra xem book đã có trong cart chưa
        Cart cart = cartRepository.findByUserIdAndBookId(userId, bookId)
                .orElse(new Cart(null, userId, bookId, 0));

        // Kiểm tra stock_quantity
        if (cart.getQuantity() + 1 > bookDTO.getStockQuantity()) {
            throw new IllegalArgumentException("Cannot add more books than available stock");
        }

        // Tăng quantity và lưu cart
        cart.setQuantity(cart.getQuantity() + 1);
        Cart savedCart = cartRepository.save(cart);

        // Trả về thông tin cart và book
        return CartResponseDTO.builder()
                .cartId(savedCart.getId())
                .userId(savedCart.getUserId())
                .bookId(savedCart.getBookId())
                .quantity(savedCart.getQuantity())
                .bookTitle(bookDTO.getTitle())
                .bookAuthor(bookDTO.getAuthor())
                .stockQuantity(bookDTO.getStockQuantity())
                .bookCategory(bookDTO.getCategory())
                .bookDescription(bookDTO.getDescription())
                .bookStatus(bookDTO.isStatus())
                .bookImageUrl(bookDTO.getImageUrl())
                .build();
    }
    @Override
    public List<CartResponseDTO> getAllBooksInCart(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);

        return cartItems.stream().map(cart -> {
            String bookServiceUrl = "http://localhost:8003/api/books/" + cart.getBookId();
            BookDTO bookDTO = restTemplate.getForObject(bookServiceUrl, BookDTO.class);

            if (bookDTO == null) {
                throw new IllegalArgumentException("Book not found for ID: " + cart.getBookId());
            }

            return CartResponseDTO.builder()
                    .cartId(cart.getId())
                    .userId(cart.getUserId())
                    .bookId(cart.getBookId())
                    .quantity(cart.getQuantity())
                    .bookTitle(bookDTO.getTitle())
                    .bookAuthor(bookDTO.getAuthor())
                    .stockQuantity(bookDTO.getStockQuantity())
                    .bookCategory(bookDTO.getCategory())
                    .bookDescription(bookDTO.getDescription())
                    .bookStatus(bookDTO.isStatus())
                    .bookImageUrl(bookDTO.getImageUrl())
                    .build();
        }).collect(Collectors.toList());
    }
}