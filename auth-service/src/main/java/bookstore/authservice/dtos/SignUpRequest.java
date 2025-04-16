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
    @NotBlank(message = "Password is required!")
    @Size(min = 8, message = "Password must have at least 8 characters!")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}",
            message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character.")
    private String password;


    @Pattern(regexp = "^(0|\\+84)(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])[0-9]{7}$",
            message = "Please input a valid Vietnamese phone number!")
    private String phoneNumber;

    private String role = "USER";
}
