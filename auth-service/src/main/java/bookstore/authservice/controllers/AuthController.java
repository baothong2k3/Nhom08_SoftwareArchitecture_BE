package bookstore.authservice.controllers;

import bookstore.authservice.dtos.SignInRequest;
import bookstore.authservice.dtos.SignUpRequest;
import bookstore.authservice.services.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth API", description = "Handle user authentication and authorization.")

public class AuthController {

    private final AccountService accountService;
    private final RestTemplate restTemplate;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(AccountService authService, AuthenticationManager authenticationManager) {
       this.accountService = authService;
       this.restTemplate = new RestTemplate();
       this.authenticationManager = authenticationManager;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest signUpRequest, BindingResult bindingResult){
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            error -> error.getField(),
                            error -> error.getDefaultMessage(),
                            (existing, replacement) -> existing, // Giữ lỗi đầu tiên nếu có trùng key
                            LinkedHashMap::new
                    ));

            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (accountService.existsByPhoneNumber(signUpRequest.getPhoneNumber())) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", Map.of("phoneNumber", "Phone number already exists!"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            ResponseEntity<?> result = accountService.signUp(signUpRequest);
            saveUserInformation(signUpRequest);
            return result;
        } catch (IllegalArgumentException ex) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }catch (Exception ex) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "An error occurred while processing the request.");
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Gửi thông tin người dùng đến API user save
     * @param signUpRequest
     */
    private void saveUserInformation(SignUpRequest signUpRequest) {
        try {
            String saveUserUrl = "http://localhost:8080/api/user/save";



            // Tạo đối tượng chứa thông tin cần gửi đi
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("phoneNumber", signUpRequest.getPhoneNumber());
            userInfo.put("enabled", true);


            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userInfo);

            // Gửi HTTP POST request đến API save
            ResponseEntity<Map> response = restTemplate.exchange(
                    saveUserUrl, HttpMethod.POST, entity, Map.class);

            // Xử lý response nếu cần
            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException("Failed to save user information: " + response.getBody());
            }
        } catch (Exception ex) {
            System.err.println("Error while saving user information: " + ex.getMessage());
        }
    }


    @PostMapping("/sign-in")
    public ResponseEntity<?> signInUser(@RequestBody @Valid SignInRequest signInRequest, BindingResult bindingResult){
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            error -> error.getField(),
                            error -> error.getDefaultMessage(),
                            (existing, replacement) -> existing, // Giữ lỗi đầu tiên nếu có trùng key
                            LinkedHashMap::new
                    ));

            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            ResponseEntity<?> result = accountService.signIn(signInRequest, authenticationManager);
            return result;
        } catch (IllegalArgumentException ex) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
