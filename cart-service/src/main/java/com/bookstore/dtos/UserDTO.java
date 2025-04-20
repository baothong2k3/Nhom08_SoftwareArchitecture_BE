package com.bookstore.cartservice.dtos;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private boolean enabled;
    private String phoneNumber;
    private LocalDate dob;
    private List<AddressDTO> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
