package com.bookstore.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    // 🔑 Khóa bí mật để ký JWT (nên lưu trong biến môi trường trong thực tế)
    private final String SECRET_KEY = "secret123";

    // ⏱️ Thời gian hết hạn của token (1 ngày)
    private final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /**
     * Tạo JWT token từ tên người dùng
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    /**
     * Lấy tên người dùng từ JWT token
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Kiểm tra tính hợp lệ của JWT token
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Giải mã và lấy thông tin từ token
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}
