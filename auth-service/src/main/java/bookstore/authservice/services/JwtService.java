package bookstore.authservice.services;

import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;
import java.util.Date;
import java.util.function.Function;

public interface JwtService {

    // Tạo token từ thông tin đăng nhập
    public String generateToken(UserDetails userDetails);

    // Trích xuất thông tin từ JWT(token)
    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction);

    // Lấy thời gian hết hạn từ JWT
    public Date extractExpiration(String token);

    // Lấy tên người dùng từ JWT
    public String extractUsername(String token);

    // Kiểm tra token có hết hạn hay không
    public Boolean validateToken(String token, UserDetails userDetails);

}
