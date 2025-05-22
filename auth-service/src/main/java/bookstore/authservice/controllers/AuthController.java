package bookstore.authservice.controllers;

import bookstore.authservice.dtos.*;
import bookstore.authservice.services.AccountService;
import bookstore.authservice.services.OtpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    private final OtpService otpService;

    @Value("http://api-gateway:8080/api/user/")
    private String urlUser;

    @Autowired
    public AuthController(AccountService authService, AuthenticationManager authenticationManager, OtpService otpService) {
        this.accountService = authService;
        this.restTemplate = new RestTemplate();
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
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

    private void saveUserInformation(SignUpRequest signUpRequest) {
        try {
            String saveUserUrl = urlUser + "/save";



            // Tạo đối tượng chứa thông tin cần gửi đi
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("phoneNumber", signUpRequest.getPhoneNumber());
            userInfo.put("enabled", true);

            // Cấu hình header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);


            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userInfo,headers);

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

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody @Valid SendOtpRequest request, BindingResult bindingResult) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            error -> error.getField(),
                            error -> error.getDefaultMessage(),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));

            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (accountService.existsByPhoneNumber(request.getPhoneNumber())) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", Map.of("phoneNumber", "Số điện thoại đã được đăng ký!"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Gửi OTP và xử lý lỗi chi tiết
        try {
            String otpStatus = otpService.generateOtp(request.getPhoneNumber());
            if (otpStatus.startsWith("Failed")) {
                response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.put("error", otpStatus);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            response.put("status", HttpStatus.OK.value());
            response.put("message", "OTP đã được gửi thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody @Valid VerifyOTPRequest request, BindingResult bindingResult) {
        Map<String, Object> response = new LinkedHashMap<>();
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            error -> error.getField(),
                            error -> error.getDefaultMessage(),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));

            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            String verificationResult = otpService.verifyOtp(request.getPhoneNumber(), request.getOtpCode());
            response.put("status", HttpStatus.OK.value());
            response.put("message", verificationResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Lỗi khi xác thực OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request, BindingResult bindingResult) {
        Map<String, Object> response = new LinkedHashMap<>();

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            error -> error.getField(),
                            error -> error.getDefaultMessage(),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));

            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }



        try {
            // Lấy username từ token đã xác thực
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Không dùng username từ request nữa
            boolean isOldPasswordValid = accountService.verifyPassword(username, request.getOldPassword());
            if (!isOldPasswordValid) {
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("message", "Mật khẩu cũ không chính xác!");
                return ResponseEntity.badRequest().body(response);
            }

            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                response.put("status", HttpStatus.BAD_REQUEST.value());
                response.put("message", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
                return ResponseEntity.badRequest().body(response);
            }

            accountService.updatePassword(username, request.getNewPassword());
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Đổi mật khẩu thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Đã xảy ra lỗi không mong muốn!");
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
