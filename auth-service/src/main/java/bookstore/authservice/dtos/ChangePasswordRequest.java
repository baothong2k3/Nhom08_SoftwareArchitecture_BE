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
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character.")
    private String oldPassword;

    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character.")
    private String newPassword;

    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character.")
    private String confirmPassword;
}
