package bookstore.authservice.controllers;

import bookstore.authservice.dtos.ApiResponse;
import bookstore.authservice.dtos.SignUpRequest;
import bookstore.authservice.services.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth API", description = "Handle user authentication and authorization.")

public class AuthController {

    private final AccountService authService;  //dùng để gọi các phương thức của AuthService

    @Autowired
    public AuthController(AccountService authService) {
       this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> registerUser(@RequestBody @Valid SignUpRequest signUpRequest, BindingResult result){
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                               .status("ERROR")
                               .message("Validation sign up failed")
                               .response(result.getAllErrors())
                               .build());
        }
        try {
            return authService.signUp(signUpRequest);
        } catch (Exception e) {
            // Bắt các ngoại lệ hệ thống khác
            return ResponseEntity.internalServerError().body(
                    ApiResponse.builder()
                            .status("ERROR")
                            .message("Internal server error")
                            .build()
            );
        }
    }


//    @PostMapping("/sign-in")
//    public ResponseEntity<ApiResponse<?>> signInUser(@RequestBody @Valid SignInRequest signInRequest){
//        return authService.signIn(signInRequest);
//    }
}
