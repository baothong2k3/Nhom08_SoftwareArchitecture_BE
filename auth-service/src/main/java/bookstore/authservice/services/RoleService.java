package bookstore.authservice.services;

import bookstore.authservice.entities.Role;

import java.util.List;

public interface RoleService{
    void saveRole(Role role);
    List<Role> getAllRoles();
    Role getRoleByCode(String roleCode);
}
