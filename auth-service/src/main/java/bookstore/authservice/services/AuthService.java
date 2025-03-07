package bookstore.authservice.services;

import bookstore.authservice.exceptions.UserAlreadyExistsException;
import bookstore.authservice.dtos.ApiResponse;
import bookstore.authservice.dtos.SignInRequest;
import bookstore.authservice.dtos.SignUpRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    //Đăng ký
    ResponseEntity<ApiResponse<?>> signUp(SignUpRequest signUpRequest)
            throws UserAlreadyExistsException;
    //Đăng nhập
    ResponseEntity<ApiResponse<?>> signIn(SignInRequest signInRequest);
}
