package bookstore.authservice.dtos;

import bookstore.authservice.entities.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Username is required!")
    @Size(min= 5, message = "Username must have at least 5 characters!")
    @Size(max= 20, message = "Username can have have at most 20 characters!")
    private String userName;

    @NotBlank(message = "Password is required!")
    @Size(min = 8, message = "Password must have at least 8 characters!")
    @Size(max = 20, message = "Password can have have at most 20 characters!")
    private String password;

    @Email(message = "Email is not in valid format!")
    @NotBlank(message = "Email is required!")
    private String email;


    private Long userId;

    private Set<Role> roles = new HashSet<>();
}
