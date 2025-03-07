package bookstore.authservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


//Kiểm tra xem người dùng có quyền truy cập vào các endpoint này không
//Nếu có quyền truy cập thì trả về message chào mừng


@RestController //Restful API Controller
@RequestMapping("/api")
public class DashboardController {

    @PreAuthorize("hasAnyRole('SCOPE_ROLE_USER','SCOPE_ROLE_ADMIN',)")  //Nhưng người có quyền truy cập
    @GetMapping("/welcome-message")
    public ResponseEntity<String> getFirstWelcomeMessage(Authentication authentication) {
        return ResponseEntity.ok(
                "Welcome to user service: " + authentication.getName() +
                        " with scope: " + authentication.getAuthorities());
    }

    @PreAuthorize("hasAnyRole('SCOPE_ROLE_ADMIN') " +
            "or hasAuthority('SCOPE_PERMISSION_UPDATE')")  //người, quyền
    @GetMapping("/admin-message")
    public ResponseEntity<String> getAdminData(Authentication authentication, Principal principal) {
        return ResponseEntity.ok("Welcome to Admin Role: " + authentication.getAuthorities());
    }
}
