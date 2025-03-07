package bookstore.authservice;

import bookstore.authservice.configs.RSAKeyRecord;
import bookstore.authservice.entities.Role;
import bookstore.authservice.enums.PermissionType;
import bookstore.authservice.enums.RoleType;
import bookstore.authservice.services.PermissionService;
import bookstore.authservice.services.RoleService;
import bookstore.authservice.services.UserService;
import bookstore.authservice.entities.Permission;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Set;

@EnableConfigurationProperties(RSAKeyRecord.class)  // Enable RSAKeyRecord
@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    // Init data (khởi tạo dữ liệu) tạo Permission, Role và User trong database.
    private void initData(
                          PermissionService permissionService,
                          RoleService roleService
                          ) {
        // Tạo các permission
        Permission permissionRead = Permission.builder()
                .code(PermissionType.PERMISSION_READ.name())
                .name(PermissionType.PERMISSION_READ.name())
                .build();
        permissionService.savePermission(permissionRead); //gọi hàm để lưu permission vào database

        Permission permissionWrite = Permission.builder()
                .code(PermissionType.PERMISSION_WRITE.name())
                .name(PermissionType.PERMISSION_WRITE.name())
                .build();
        permissionService.savePermission(permissionWrite);

        Permission permissionDelete = Permission.builder()
                .code(PermissionType.PERMISSION_DELETE.name())
                .name(PermissionType.PERMISSION_DELETE.name())
                .build();
        permissionService.savePermission(permissionDelete);

        Permission permissionUpdate = Permission.builder()
                .code(PermissionType.PERMISSION_UPDATE.name())
                .name(PermissionType.PERMISSION_UPDATE.name())
                .build();
        permissionService.savePermission(permissionUpdate);


        // Tạo các role
        Role roleGuest = Role.builder()
                .code(RoleType.ROLE_GUEST.name())
                .name(RoleType.ROLE_GUEST.name())
                .permissions(Set.of(permissionRead,permissionWrite)) // Guest chỉ có quyền đọc
                .build();
        roleService.saveRole(roleGuest);

        Role roleUser = Role.builder()
                .code(RoleType.ROLE_USER.name())
                .name(RoleType.ROLE_USER.name())
                .permissions(Set.of(permissionRead, permissionWrite)) // User có quyền đọc và ghi
                .build();
        roleService.saveRole(roleUser);

        Role roleAdmin = Role.builder()
                .code(RoleType.ROLE_ADMIN.name())
                .name(RoleType.ROLE_ADMIN.name())
                .permissions(Set.of(permissionRead, permissionWrite, permissionUpdate, permissionDelete)) // Admin full quyền
                .build();
        roleService.saveRole(roleAdmin);
    }

    //Chạy sau khi Spring Boot khởi động xong (nó sẽ tạo dữ liệu trong database)
    @Bean
    public CommandLineRunner runner(PasswordEncoder passwordEncoder, PermissionService permissionService, RoleService roleService, UserService userService) {
        return (args) -> {
            initData(permissionService, roleService);
        };
    }
}
