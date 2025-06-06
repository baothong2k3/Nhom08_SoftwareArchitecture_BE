package bookstore.authservice.services.impl;

import bookstore.authservice.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.security.Key;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;


@Component
public class JwtServiceImpl implements JwtService {

    public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";


    @Override
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities())
                .setIssuedAt(new Date()) // Thời gian phát hành token
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000)) // Hết hạn sau 30 phút
                .signWith(SignatureAlgorithm.HS256, SECRET) // Ký token với khóa bí mật
                .compact(); // Tạo chuỗi token hoàn chỉnh
    }


    /**
     * Trích xuất thời gian hết hạn từ token
     * @param token
     * @return
     */
    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    /**
     * Trích xuất Subject từ token
     * @param token
     * @return
     */
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    /**
     * Phương thức generic để trích xuất bất kỳ thông tin nào từ token dựa trên một hàm resolver.
     * @param token
     * @param claimsResolver
     * @return
     * @param <T>
     */
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);  // Mở hộp token ra
        return claimsResolver.apply(claims);  // Lấy ra thông tin cần thiết
    }


    /**
     * Giải mã token để lấy toàn bộ claims (thông tin bên trong token).
     * @param token
     * @return
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)  // Xác thực bằng secret key
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Kiểm tra xem token đã hết hạn hay chưa
     * @param token
     * @return
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Kiểm tra tính hợp lệ của token
     * @param token
     * @param userDetails
     * @return
     */
    @Override
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String tenDangNhap = extractUsername(token);
        return (tenDangNhap.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
