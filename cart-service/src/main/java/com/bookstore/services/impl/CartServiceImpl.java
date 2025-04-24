package com.bookstore.services.impl;

import com.bookstore.dtos.BookDTO;
import com.bookstore.dtos.CartResponseDTO;
import com.bookstore.entities.Cart;
import com.bookstore.repositories.CartRepository;
import com.bookstore.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
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
        String bookServiceUrl = "http://localhost:8003/api/books/" + bookId;
        BookDTO bookDTO = restTemplate.getForObject(bookServiceUrl, BookDTO.class);

        if (bookDTO == null || !bookDTO.isStatus()) {
            throw new IllegalArgumentException("Book not found or unavailable");
        }

        Cart cart = cartRepository.findByUserIdAndBookId(userId, bookId)
                .orElse(new Cart(null, userId, bookId, 0));

        if (cart.getQuantity() + 1 > bookDTO.getStockQuantity()) {
            throw new IllegalArgumentException("Cannot add more books than available stock");
        }

        cart.setQuantity(cart.getQuantity() + 1);
        Cart savedCart = cartRepository.save(cart);

        BigDecimal totalPrice = bookDTO.getPrice().multiply(BigDecimal.valueOf(savedCart.getQuantity()));

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
                .price(bookDTO.getPrice())
                .totalPrice(totalPrice)
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

            BigDecimal totalPrice = bookDTO.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));

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
                    .price(bookDTO.getPrice())
                    .totalPrice(totalPrice)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public void removeBookFromCart(Long userId, Long bookId) {
        Cart cart = cartRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found for userId: " + userId + " and bookId: " + bookId));
        cartRepository.delete(cart);
    }
    @Override
    public void increaseBookQuantity(Long userId, Long bookId) {
        Cart cart = cartRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found for userId: " + userId + " and bookId: " + bookId));

        String bookServiceUrl = "http://localhost:8003/api/books/" + bookId;
        BookDTO bookDTO = restTemplate.getForObject(bookServiceUrl, BookDTO.class);

        if (bookDTO == null || !bookDTO.isStatus()) {
            throw new IllegalArgumentException("Book not found or unavailable");
        }

        if (cart.getQuantity() + 1 > bookDTO.getStockQuantity()) {
            cart.setQuantity(bookDTO.getStockQuantity());
        } else {
            cart.setQuantity(cart.getQuantity() + 1);
        }

        cartRepository.save(cart);
    }

    @Override
    public void decreaseBookQuantity(Long userId, Long bookId) {
        Cart cart = cartRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found for userId: " + userId + " and bookId: " + bookId));

        if (cart.getQuantity() - 1 <= 0) {
            cartRepository.delete(cart);
        } else {
            cart.setQuantity(cart.getQuantity() - 1);
            cartRepository.save(cart);
        }
    }
    @Override
    public void clearCart(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(cartItems);
    }
}