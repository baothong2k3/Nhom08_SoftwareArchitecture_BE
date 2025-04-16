package com.bookstore.configs;


import com.bookstore.filters.JWTGlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private final JWTGlobalFilter jwtGlobalFilter;

    // Inject filter qua constructor
    public SecurityConfig(JWTGlobalFilter jwtGlobalFilter) {
        this.jwtGlobalFilter = jwtGlobalFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        http.csrf(csrfSpec -> csrfSpec.disable())
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers("/api/auth/sign-up", "/api/auth/sign-in").permitAll()
                        .pathMatchers("/products/**", "/orders/**", "/customers/**").authenticated()
                        .anyExchange().permitAll()
                )
                .addFilterBefore(jwtGlobalFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }
}
