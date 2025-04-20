package bookstore.authservice.services;


import bookstore.authservice.dtos.SignInRequest;
import bookstore.authservice.dtos.SignUpRequest;
import bookstore.authservice.entities.Account;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface AccountService extends UserDetailsService {
    //Đăng ký
    ResponseEntity<?> signUp(SignUpRequest signUpRequest);


    //Đăng nhập
    ResponseEntity<?> signIn(SignInRequest signInRequest, AuthenticationManager authenticationManager);

    public boolean existsByPhoneNumber(String phoneNumber);

    boolean verifyPassword(@NotBlank(message = "Username is required!") String username, @NotBlank(message = "Old password is required!") String oldPassword);

    void updatePassword(@NotBlank(message = "Username is required!") String username, @NotBlank(message = "New password is required!") String newPassword);
}
