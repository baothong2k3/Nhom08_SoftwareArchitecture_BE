package bookstore.authservice.services.impl;

import bookstore.authservice.entities.Role;
import bookstore.authservice.repositories.RoleRepository;
import bookstore.authservice.services.RoleService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    @Modifying //đánh dấu phương thức này sẽ thay đổi dữ liệu trong cơ sở dữ liệu
    @Override
    public void saveRole(Role role) {
        roleRepository.save(role);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }


}
