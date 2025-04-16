package bookstore.userservice.dtos;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserDTO {
    private Long id;
    @NotBlank(message = "Full name is required!")
    @Size(min= 5, message = "Username must have at least 5 characters!")
    @Size(max= 20, message = "Username can have have at most 20 characters!")
    private String fullName;

    @Email(message = "Email is not in valid format!")
    @NotBlank(message = "Email is required!")
    private String email;
    private boolean enabled;


    @Pattern(regexp = "^(0|\\+84)(3[2-9]|5[2689]|7[0-9]|8[1-9]|9[0-9])[0-9]{7}$",
            message = "Please input a valid Vietnamese phone number!")
    private String phoneNumber;


    @Past(message = "Date of birth must be less than today")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private List<AddressDTO> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @AssertTrue(message = "User must be at least 13 years old")
    public boolean isOlderThan13() {
        if (dob == null) {
            return true;
        }
        return Period.between(dob, LocalDate.now()).getYears() >= 13;
    }
}
