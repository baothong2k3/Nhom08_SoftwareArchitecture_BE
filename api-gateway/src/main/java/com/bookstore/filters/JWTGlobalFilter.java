package com.bookstore.filters;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *  Xử lý xác thực JWT
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JWTGlobalFilter implements WebFilter {


    private static final String SECRET_KEY = "6d7f6e6f4f3a9f97f2616c740213adf6a3acfb9f5b7178ab8f12f5d531e98d3a";


    /**
     * Trích xuất token JWT từ header Authorization của yêu cầu.
     * @param exchange
     * @return
     */
    private String extractJwtFromRequest(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Giải mã token JWT để lấy thông tin (claims) bên trong.
     * @param token
     * @return
     */
    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
        catch (JwtException e) {
            return null;
        }
    }

    /**
     * Trích xuất danh sách quyền (roles/authorities) từ claims và chuyển thành SimpleGrantedAuthority (định dạng Spring Security hiểu được).
     * @param claims
     * @return
     */
    private List<SimpleGrantedAuthority> extractAuthoritiesFromClaims(Claims claims) {
        Object rolesObject = claims.get("roles");

        if (rolesObject instanceof String) {
            // Trường hợp quyền chỉ có 1 giá trị duy nhất (String)
            return Collections.singletonList(new SimpleGrantedAuthority((String) rolesObject));
        } else if (rolesObject instanceof List) {
            // Trường hợp quyền là danh sách (nếu người dùng có thể có nhiều quyền)
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
            // Trường hợp quyền không hợp lệ
            return Collections.emptyList();
        }
    }

    /**
     * Xử lý logic chính của filter, xác thực token và thiết lập thông tin bảo mật.
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        //Bỏ qua các yêu cầu đến đường dẫn /auth/login và /auth/register
        if (exchange.getRequest().getURI().toString().contains("/auth/login") ||
                exchange.getRequest().getURI().toString().contains("/auth/register")) {

            return chain.filter(exchange);  //chuyển tiếp yêu cầu
        }

        //Lấy token từ yêu cầu và giải mã claims
        String token = extractJwtFromRequest(exchange);
        Claims claims = extractClaims(token);


        // Kiểm tra token
        if (token == null || claims == null) {
            return Mono.error(new JwtException("Invalid or missing JWT token"));
        }

        // Tạo danh sách quyền
        List<SimpleGrantedAuthority> authorities = extractAuthoritiesFromClaims(claims);

        // Tạo đối tượng Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null, authorities
        );



        // Create an empty SecurityContext and set the authentication object
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();  // lưu trữ thông tin bảo mật
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Set token to header
        exchange.getRequest().mutate().header("Authorization", "Bearer " + token);

        // Save the SecurityContext in the session using SecurityContextRepository
        return exchange.getSession()
                .flatMap(session -> {
                    session.getAttributes().put("SPRING_SECURITY_CONTEXT", securityContext);
                    return chain.filter(exchange);
                });
    }
}
