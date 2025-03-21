package bookstore.authservice.services;


import bookstore.authservice.dtos.ApiResponse;
import bookstore.authservice.dtos.SignUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends UserDetailsService {
    //Đăng ký
    ResponseEntity<ApiResponse<?>> signUp(SignUpRequest signUpRequest);

    //Đăng nhập
//    ResponseEntity<ApiResponse<?>> signIn(SignInRequest signInRequest);

//    public ResponseEntity<?> signIn(SignInRequest account, AuthenticationManager authenticationManager)
}
