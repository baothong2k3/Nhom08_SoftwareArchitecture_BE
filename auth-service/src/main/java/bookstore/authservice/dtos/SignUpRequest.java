package bookstore.authservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class SignUpRequest {
    @NotBlank(message = "Vui lòng nhập mật khẩu!")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 kí tự!")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}",
            message = "Mật khẩu phải chứa ít nhất một chữ cái viết thường, một chữ cái viết hoa, một chữ số và một ký tự đặc biệt.")
    private String password;


    @NotBlank(message = "Vui lòng nhập số điện thoại!")
    @Pattern(regexp = "^(0|\\+84)(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])[0-9]{7}$",
            message = "Số điện thoại phải bắt đầu bằng 0 hoặc +84 và có 10 chữ số hợp lệ tại Việt Nam!")
    private String phoneNumber;

    private String role = "USER";
}
