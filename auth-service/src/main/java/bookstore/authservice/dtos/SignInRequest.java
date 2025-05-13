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
    @NotBlank(message = "Vui lòng nhập mật khẩu!")
    private String password;

    @NotBlank(message = "Vui lòng nhập email hoặc số điện thoại!")
    private String username;
}
