package com.bookstore.configs;

import com.bookstore.filters.JWTGlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.bookstore.configs.SecurityConstants.PUBLIC_PATHS;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private final JWTGlobalFilter jwtGlobalFilter;

    public SecurityConfig(JWTGlobalFilter jwtGlobalFilter) {
        this.jwtGlobalFilter = jwtGlobalFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(authorize -> authorize
                        .pathMatchers(PUBLIC_PATHS).permitAll()
                        .pathMatchers("/api/cart/**", "/orders/**", "/customers/**").authenticated()
                        .anyExchange().permitAll()
                )
                // Quan trọng: Sử dụng NoOpServerSecurityContextRepository để không lưu context (stateless JWT)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .addFilterAt(jwtGlobalFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, exception) -> {
                            var response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.UNAUTHORIZED);
                            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                            String errorJson = "{\"error\":\"Vui lòng đăng nhập để truy cập\"}";
                            byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);

                            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
                        })
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:5500", "http://127.0.0.1:5500"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}