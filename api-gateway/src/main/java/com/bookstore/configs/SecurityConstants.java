package com.bookstore.configs;

public class SecurityConstants {
    public static final String[] PUBLIC_PATHS = {
            "/api/auth/sign-up",
            "/api/auth/sign-in",
            "/api/auth/send-otp",
            "/api/auth/verify-otp",
            "/api/auth/forgot-password",
            "/api/books/paged",
            "/api/books/*/check-stock",
    };

    public static final String[] ADMIN_ONLY_PATHS = { "/user/all" };

    public static final String[] TOKEN_PATHS = {
            "/api/cart/",
            "/api/orders/",
            "/customers/",
            "/api/user/get",
            "/api/user/add-address",
            "/api/orders/*/details",
            "/api/user/update",
            "/api/orders/*/cancel",
            "/api/books/*/increase-stock"
    };
}
