package bookstore.authservice.dtos;

import bookstore.authservice.entities.Role;

import java.util.Set;

public class AccountRequestDTO {
    private String userName;
    private Long userId;
    private boolean isActive;
    private boolean isLocked;
    private Set<Role> roles;
}
