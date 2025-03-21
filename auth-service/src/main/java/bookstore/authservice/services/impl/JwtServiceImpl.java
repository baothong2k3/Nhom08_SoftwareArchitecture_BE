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

    // Tạo khóa bí mật
    private Key getSigneKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET); // Chuyển chuỗi secret thành mảng byte
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateToken(String tenDangNhap, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", tenDangNhap);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims) // Đính kèm các thông tin thêm (role, isAdmin, ...)
                .setSubject(tenDangNhap) // Ghi tên đăng nhập vào token
                .setIssuedAt(new Date(System.currentTimeMillis())) // Thời gian phát hành token
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000)) // Hết hạn sau 30 phút
                .signWith(SignatureAlgorithm.HS256, getSigneKey()) // Ký token với khóa bí mật
                .compact(); // Tạo chuỗi token hoàn chỉnh
    }


    // Trích xuất thời gian hết hạn từ token
    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Trích xuất tên người dùng từ token
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);  // Mở hộp token ra
        return claimsResolver.apply(claims);  // Lấy ra thông tin cần thiết
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigneKey())  // Xác thực bằng secret key
                .parseClaimsJws(token)
                .getBody();
    }

    // Kiểm tra token có hết hạn hay không
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Kiểm tra token có hợp lệ hay không
    @Override
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String tenDangNhap = extractUsername(token);  // Lấy tên người dùng từ token
        return (tenDangNhap.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
