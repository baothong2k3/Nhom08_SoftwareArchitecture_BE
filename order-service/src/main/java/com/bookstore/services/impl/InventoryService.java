package com.bookstore.services.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryService {
    @Autowired
    private RestTemplate restTemplate;

    @CircuitBreaker(name = "bookServiceCircuitBreaker", fallbackMethod = "fallbackForCheckStock")
    @Retry(name = "bookServiceRetry")
    @RateLimiter(name = "bookServiceRateLimiter")
    public Boolean checkStock(Long bookId, Integer quantity) {
        String url = "http://localhost:8080/api/books/" + bookId + "/check-stock?requestedQuantity=" + quantity;
        try {
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API kiểm tra kho sách: " + e.getMessage());
            throw e; // Ensure exception propagates to trigger circuit breaker
        }
    }


    @CircuitBreaker(name = "bookServiceCircuitBreaker", fallbackMethod = "fallbackForUpdateStock")
    @Retry(name = "bookServiceRetry")
    @RateLimiter(name = "bookServiceRateLimiter")
    public void updateStock(String bookServiceUrl, Integer quantity) {
        try {
            restTemplate.patchForObject(bookServiceUrl, quantity, Void.class);
        } catch (Exception e) {
            throw e; // Ensure exception propagates to trigger circuit breaker
        }
    }


    private Boolean fallbackForCheckStock(Long bookId, Integer quantity, Throwable t) {
        System.err.println("⚠️ [Fallback] Lỗi khi kiểm tra kho sách:");
        System.err.println("🔹 Nguyên nhân lỗi: " + t.getClass().getSimpleName() + " - " + t.getMessage());
        return false;
    }



    private void fallbackForUpdateStock(String bookServiceUrl, Integer quantity, Throwable t) {
        System.err.println("❌ [Fallback] Lỗi khi cập nhật số lượng tồn kho:");
        System.err.println("🔹 Nguyên nhân lỗi: " + t.getClass().getSimpleName() + " - " + t.getMessage());
        throw new RuntimeException("Không thể cập nhật số lượng tồn. Vui lòng thử lại sau.");
    }
}
