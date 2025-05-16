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
    @NotBlank(message = "Password is required!")
    private String password;

    @NotBlank(message = "Username is required!")
    private String username;


}
