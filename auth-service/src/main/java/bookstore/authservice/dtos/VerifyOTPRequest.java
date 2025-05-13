package bookstore.authservice.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOTPRequest {
    @NotBlank(message = "Vui lòng nhập số điện thoại!")
    @Pattern(regexp = "^(0|\\+84)(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])[0-9]{7}$",
            message = "Số điện thoại phải bắt đầu bằng 0 hoặc +84 và có 10 chữ số hợp lệ tại Việt Nam!")
    private String phoneNumber;

    @NotBlank(message = "Vui lòng nhập mã OTP!")
    @Pattern(regexp = "^[0-9]{6}$", message = "Mã OTP phải gồm 6 chữ số!")
    private String otpCode;
}
