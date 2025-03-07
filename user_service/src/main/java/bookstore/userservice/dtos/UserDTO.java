package bookstore.userservice.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    @NotBlank(message = "Username is required!")
    @Size(min= 5, message = "Username must have at least 5 characters!")
    @Size(max= 20, message = "Username can have have at most 20 characters!")
    private String userName;

    @Email(message = "Email is not in valid format!")
    @NotBlank(message = "Email is required!")
    private String email;
    private boolean enabled;

    @NotBlank(message = "Password is required!")
    private String password;

    private Long role;

    @Pattern(regexp = "\\(\\d{3}\\)\\d{3}-\\d{4}",
            message = "Please input phone number with format: (NNN)NNN-NNNN")
    private String phoneNumber;

    @Past(message = "Date of birth must be less than today")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private List<AddressDTO> addresses;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
