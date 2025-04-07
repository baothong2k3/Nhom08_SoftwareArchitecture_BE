package bookstore.authservice.configs;

public class Endpoints {

    public static final String[] PUBLIC_GET_ENDPOINS = {
            "/api/account/search/existsByTenDangNhap",
            "/api/account/search/existsByEmail",
            "/swagger-ui/**",      // Cho phép Swagger UI
            "/v3/api-docs/**",     // Cho phép OpenAPI
            "/swagger-resources/**",
            "/webjars/**",
    };

    public static final String[] PUBLIC_POST_ENDPOINS = {
            "api/auth/sign-up",
            "/api/account/sign-in",
    };

    public static final String[] ADMIN_GET_ENDPOINS = {
            "/api/account",
            "/api/account/**",
    };
}
