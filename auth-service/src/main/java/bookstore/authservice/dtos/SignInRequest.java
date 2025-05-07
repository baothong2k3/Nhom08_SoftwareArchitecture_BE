package bookstore.authservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class SignInRequest {
    @NotBlank(message = "Mật khẩu là bắt buộc!")
    private String password;

    @NotBlank(message = "Tên đăng nhập là bắt buộc!")
    private String username;

}
