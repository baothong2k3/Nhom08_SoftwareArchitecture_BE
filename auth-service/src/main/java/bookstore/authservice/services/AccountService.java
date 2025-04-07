package bookstore.authservice.services;


import bookstore.authservice.dtos.SignInRequest;
import bookstore.authservice.dtos.SignUpRequest;
import bookstore.authservice.entities.Account;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface AccountService extends UserDetailsService {
    //Đăng ký
    ResponseEntity<?> signUp(SignUpRequest signUpRequest);

    //Đăng nhập
    ResponseEntity<?> signIn(SignInRequest signInRequest);

    public boolean existsByPhoneNumber(String phoneNumber);
}
