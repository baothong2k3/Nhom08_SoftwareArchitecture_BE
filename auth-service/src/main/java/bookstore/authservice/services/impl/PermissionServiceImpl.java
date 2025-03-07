package bookstore.authservice.services.impl;

import bookstore.authservice.repositories.PermissionRepository;
import bookstore.authservice.services.PermissionService;
import bookstore.authservice.entities.Permission;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {
    private PermissionRepository permissionRepository;

    @Autowired
    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    @Modifying
    @Override
    public void savePermission(Permission permission) {
        permissionRepository.save(permission);
    }
}
