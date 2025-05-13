package com.bookstore.filters;

import com.bookstore.configs.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Xử lý xác thực JWT
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JWTGlobalFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTGlobalFilter.class);
    private static final String SECRET_KEY = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    // Phương thức trích xuất và xử lý token vẫn giữ nguyên
    private String extractJwtFromRequest(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        log.warn("Missing or invalid Authorization header: {}", bearerToken);
        return null;
    }

    private Claims extractClaims(String token) {
        // Giữ nguyên phương thức
        if (token == null || token.isEmpty()) {
            log.warn("JWT token is null or empty");
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.getExpiration().before(new java.util.Date())) {
                log.error("JWT token has expired: {}", token);
                return null;
            }

            return claims;
        } catch (JwtException e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }

    private List<SimpleGrantedAuthority> extractAuthoritiesFromClaims(Claims claims) {
        // Giữ nguyên phương thức
        Object rolesObject = claims.get("role");

        if (rolesObject instanceof String) {
            return Collections.singletonList(new SimpleGrantedAuthority((String) rolesObject));
        } else if (rolesObject instanceof List) {
            List<String> roles = ((List<?>) rolesObject).stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> ((Map<?, ?>) item).get("authority"))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());

            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private boolean isPublicPath(String path) {
        // Kiểm tra xem đường dẫn có bắt đầu bằng một trong các đường dẫn công khai
        for (String publicPath : SecurityConstants.PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true; // Nếu là public path, không cần xác thực
            }
        }
        return false; // Nếu không phải, cần xác thực
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        log.info("Processing request for path: {}", path);

        // Sử dụng phương thức isPublicPath để kiểm tra xem đường dẫn có phải là public path không
        if (isPublicPath(path)) {
            return chain.filter(exchange); // Nếu là public path, không cần xác thực
        }

        // Yêu cầu token cho các endpoint bảo mật
        if (path.startsWith("/api/cart/") || path.startsWith("/api/orders/") || path.startsWith("/customers/")) {

            String token = extractJwtFromRequest(exchange);

            // Kiểm tra token có tồn tại không
            if (token == null) {
                return handleUnauthorized(exchange, "Vui lòng đăng nhập để thực hiện hành động này");
            }

            // Trích xuất thông tin từ token
            Claims claims = extractClaims(token);
            if (claims == null) {
                return handleUnauthorized(exchange, "Token không hợp lệ hoặc đã hết hạn");
            }

            // Lấy userId từ claims
            Object userIdObj = claims.get("userId");
            if (userIdObj == null) {
                return handleUnauthorized(exchange, "Không tìm thấy thông tin người dùng trong token");
            }

            // Chuyển userId thành Long
            Long userId;
            try {
                userId = Long.valueOf(userIdObj.toString());
            } catch (NumberFormatException e) {
                return handleUnauthorized(exchange, "Định dạng ID người dùng không hợp lệ");
            }

            // Tạo danh sách quyền
            List<SimpleGrantedAuthority> authorities = extractAuthoritiesFromClaims(claims);

            // Tạo đối tượng Authentication
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null, authorities
            );

            log.info("User authenticated: {}, userId: {}", claims.getSubject(), userId);

            // Thêm token và userId vào header của request
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("Authorization", "Bearer " + token)
                    .header("UserId", userId.toString())
                    .build();

            // Đặt xác thực vào ReactiveSecurityContextHolder
            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        }
        // Cho phép yêu cầu đi tiếp nếu không phải endpoint bảo mật
        return chain.filter(exchange);
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorJson = "{\"error\":\"" + message + "\"}";
        byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}