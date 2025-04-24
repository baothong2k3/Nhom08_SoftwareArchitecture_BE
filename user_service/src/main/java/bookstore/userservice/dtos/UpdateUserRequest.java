/*
 * @ (#) UpdateUserRequest.java    1.0    24/04/2025
 * Copyright (c) 2025 IUH. All rights reserved.
 */
package bookstore.userservice.dtos;/*
 * @description:
 * @author: Bao Thong
 * @date: 24/04/2025
 * @version: 1.0
 */

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    @NotBlank(message = "Full name is required!")
    @Size(min = 5, message = "Full name must have at least 5 characters!")
    @Size(max = 50, message = "Full name can have at most 50 characters!")
    private String fullName;

    @Email(message = "Email is not in valid format!")
    @NotBlank(message = "Email is required!")
    private String email;

    @Past(message = "Date of birth must be in the past!")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
}
