package com.bookstore.entities;

public enum OrderStatus {
    PLACED, // Đã đặt hàng
    CONFIRMED, // Đã nhận đơn hàng
    SHIPPING, // Đang vận chuyển
    DELIVERED, // Đã giao hàng
    CANCELED // Đã hủy
}