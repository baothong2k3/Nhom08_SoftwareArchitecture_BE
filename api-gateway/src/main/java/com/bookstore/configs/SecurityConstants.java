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
            "/api/cart/add",
            "/api/cart/all",
            "/api/cart/increase",
            "/api/cart/decrease",
            "/api/cart/remove",
            "/api/orders/",
            "/api/orders/place",
            "/api/orders/user",
            "/customers/",
            "/api/user/get",
            "/api/user/update",
            "/api/user/add-address",
            "/api/orders/*/details",
            "/api/orders/*/cancel",
            "/api/books/*/increase-stock",
            "/api/books/*/update",
            "/api/auth/change-password"
    };
}
