package com.bookstore.configs;

public class SecurityConstants {
    public static final String[] PUBLIC_PATHS = {
            "/api/auth/sign-up",
            "/api/auth/sign-in",
            "/api/auth/send-otp",
            "/api/auth/verify-otp",
            "/api/auth/forgot-password",
            "/api/books/paged",
    };
}
