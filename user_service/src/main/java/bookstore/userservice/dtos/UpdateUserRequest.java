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
    private String fullName;
    private String email;
    private LocalDate dob;
}
