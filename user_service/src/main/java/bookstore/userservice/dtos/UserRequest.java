package bookstore.userservice.dtos;


import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @Pattern(regexp = "^(0|\\+84)(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])[0-9]{7}$",
            message = "Please input a valid Vietnamese phone number!")
    @NotBlank(message = "Phone number is required!")
    private String phoneNumber;
    private boolean enabled;
}
