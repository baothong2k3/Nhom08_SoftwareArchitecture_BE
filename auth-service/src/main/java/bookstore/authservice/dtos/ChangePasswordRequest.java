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
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class ChangePasswordRequest {
    @NotBlank(message = "Old password is required!")
    private String oldPassword;

    @NotBlank(message = "New password is required!")
    private String newPassword;

    @NotBlank(message = "Confirm password is required!")
    private String confirmPassword;
}
