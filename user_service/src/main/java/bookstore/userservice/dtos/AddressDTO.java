package bookstore.userservice.dtos;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {

    @NotBlank(message = "Address is required")
    private String address;

    private Long id;

    @JsonIgnore
    private UserDTO user;
}
