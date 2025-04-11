package bookstore.userservice.services;

import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.dtos.UserRequest;
import java.util.List;

public interface UserService {
    
    // Tạo hoặc cập nhật user
    UserRequest save(UserRequest userRequest);

    // Kiểm tra tồn tại
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);

    // Truy vấn user
    UserDTO findById(Long id);
    List<UserDTO> findAll();

    // ✅ Hàm lấy user từ JWT token
    UserDTO getUserFromToken(String token);
}
