package bookstore.authservice.dtos;

public class JwtRespone {
    private final String token;

    public JwtRespone(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
