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
    @NotBlank(message = "Mật khẩu là băt buộc!")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 kí tự!")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}",
            message = "Mật khẩu phải chứa ít nhất một chữ cái thường, một chữ cái in hoa, một chữ số và một ký tự đặc biệt.")
    private String password;


    @Pattern(regexp = "^(0|\\+84)(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])[0-9]{7}$",
            message = "Số điện thoại không hợp lệ!")
    private String phoneNumber;


    @NotBlank(message = "Mã xác thực là bắt buộc!")
    private String otp;

    private String role = "USER";
}
