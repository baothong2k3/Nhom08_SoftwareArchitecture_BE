package bookstore.authservice.controllers;

import bookstore.authservice.exceptions.UserAlreadyExistsException;
import bookstore.authservice.dtos.ApiResponse;
import bookstore.authservice.dtos.SignInRequest;
import bookstore.authservice.dtos.SignUpRequest;
import bookstore.authservice.services.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);  //ghi log
    private final AuthService authService;  //dùng để gọi các phương thức của AuthService

    @Autowired
    public AuthController(AuthService authService) {
       this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> registerUser(@RequestBody @Valid SignUpRequest signUpRequest)  //RequestBody: chuyển dữ liệu từ request thành object
            throws UserAlreadyExistsException {  //ném ra ngoại lệ nếu user đã tồn tại
        return authService.signUp(signUpRequest);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<?>> signInUser(@RequestBody @Valid SignInRequest signInRequest){
        return authService.signIn(signInRequest);
    }
}
