package bookstore.userservice.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class UserReponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private List<String> addresses;
}
