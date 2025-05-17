/*
 * @ (#) ChangePasswordRequest.java    1.0    20/04/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package bookstore.authservice.dtos;/*
 * @description:
 * @author: Bao Thong
 * @date: 20/04/2025
 * @version: 1.0
 */

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class ChangePasswordRequest {
    @NotBlank(message = "Vui lòng nhập mật khẩu cũ!")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}",
            message = "Mật khẩu phải chứa ít nhất một chữ cái viết thường, một chữ cái viết hoa, một chữ số và một ký tự đặc biệt.")
    private String oldPassword;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới!")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}",
            message = "Mật khẩu phải chứa ít nhất một chữ cái viết thường, một chữ cái viết hoa, một chữ số và một ký tự đặc biệt.")
    private String newPassword;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu mới!")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}",
            message = "Mật khẩu phải chứa ít nhất một chữ cái viết thường, một chữ cái viết hoa, một chữ số và một ký tự đặc biệt.")
    private String confirmPassword;
}
