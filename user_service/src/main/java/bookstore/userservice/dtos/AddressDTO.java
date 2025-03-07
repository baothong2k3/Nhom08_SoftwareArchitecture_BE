package bookstore.userservice.dtos;

import bookstore.userservice.entities.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {

    private Long id;

    private String address;

    private User user;
}
