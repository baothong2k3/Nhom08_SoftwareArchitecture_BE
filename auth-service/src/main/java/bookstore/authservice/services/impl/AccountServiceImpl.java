package bookstore.authservice.services.impl;


<<<<<<< HEAD
import bookstore.authservice.dtos.JwtRespone;
=======
>>>>>>> 70eb395 (create docker)
import bookstore.authservice.dtos.SignInRequest;
import bookstore.authservice.entities.Account;
import bookstore.authservice.repositories.AccountRepository;
import bookstore.authservice.services.AccountService;
import bookstore.authservice.dtos.SignUpRequest;
import bookstore.authservice.services.JwtService;
<<<<<<< HEAD
import org.modelmapper.ModelMapper;
=======
>>>>>>> 70eb395 (create docker)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
<<<<<<< HEAD
=======
import org.springframework.security.authentication.BadCredentialsException;
>>>>>>> 70eb395 (create docker)
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
<<<<<<< HEAD
=======
import org.springframework.security.core.GrantedAuthority;
>>>>>>> 70eb395 (create docker)

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
<<<<<<< HEAD

    private final JwtService jwtService;
    private final ModelMapper modelMapper;

=======
    private final JwtService jwtService;
>>>>>>> 70eb395 (create docker)

    @Autowired
    public AccountServiceImpl(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
<<<<<<< HEAD
            JwtService jwtService,
            ModelMapper modelMapper) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.modelMapper = modelMapper;
    }


=======
            JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

>>>>>>> 70eb395 (create docker)
    @Override
    public ResponseEntity<?> signUp(SignUpRequest signUpRequest) {
        Map<String, Object> response = new LinkedHashMap<>();


        // Mã hóa mật khẩu trước khi lưu vào database
        String encryptPassword = passwordEncoder.encode(signUpRequest.getPassword());
        signUpRequest.setPassword(encryptPassword);

        // Chuyển SignUpRequest thành Account
        Account account = new Account();
        account.setPhoneNumber(signUpRequest.getPhoneNumber());
        account.setPassword(signUpRequest.getPassword());
        account.setRole(signUpRequest.getRole());

        accountRepository.save(account);

        response.put("status", HttpStatus.CREATED.value());
        response.put("message", "Account created successfully!");
        response.put("data", account);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<?> signIn(SignInRequest signInRequest, AuthenticationManager authenticationManager) {
        Map<String, Object> response = new LinkedHashMap<>();
<<<<<<< HEAD
        try {
            // Xác định người dùng nhập email hay số điện thoại
            String identifier = (signInRequest.getEmail() != null && !signInRequest.getEmail().isEmpty())
                    ? signInRequest.getEmail()
                    : signInRequest.getPhoneNumber();

            if (identifier == null || signInRequest.getPassword() == null) {
                response.put("success", false);
                response.put("message", "Email/Phone number and password cannot be blank.");
                return ResponseEntity.badRequest().body(response);
            }

            // Xác thực người dùng
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, signInRequest.getPassword()));

            // Nếu xác thực thành công, tạo JWT token
            if (authentication.isAuthenticated()) {
                final String token = jwtService.generateToken((UserDetails) authentication.getPrincipal()); // Tạo JWT token
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("token", token);

                return ResponseEntity.ok(response);
            }
        } catch (AuthenticationException e) {
            response.put("success", false);
            response.put("message", "Incorrect login information.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        response.put("success", false);
        response.put("message", "Login failed due to an unknown error.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

=======
        Map<String, String> errorMap = new LinkedHashMap<>();

        String username = signInRequest.getUsername();
        String password = signInRequest.getPassword();

        try {
            // Load UserDetails (gây UsernameNotFoundException nếu không tìm thấy)
            UserDetails userDetails = loadUserByUsername(username);

            // Xác thực người dùng
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDetails.getUsername(), password));

            // Nếu xác thực thành công → tạo token + role
            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(userDetails);
                String role = userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .orElse("ROLE_USER");

                response.put("success", true);
                response.put("message", "Đăng nhập thành công");
                response.put("token", token);
                response.put("role", role);
                return ResponseEntity.ok(response);
            }

        } catch (AuthenticationException e) {
            if (e instanceof BadCredentialsException) {
                errorMap.put("password", "Mật khẩu không đúng. Vui lòng thử lại.");
            } else if (e instanceof UsernameNotFoundException) {
                errorMap.put("username", "Tài khoản không tồn tại trong hệ thống.");
            } else {
                errorMap.put("message", "Xác thực thất bại. Vui lòng kiểm tra lại thông tin.");
            }
        } catch (Exception e) {
            errorMap.put("message", "Đã xảy ra lỗi trong quá trình đăng nhập.");
            return buildErrorResponse(errorMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Nếu có lỗi, trả về tất cả lỗi cùng lúc
        return buildErrorResponse(errorMap, HttpStatus.UNAUTHORIZED);
    }


>>>>>>> 70eb395 (create docker)
    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return accountRepository.existsByPhoneNumber(phoneNumber);
    }

<<<<<<< HEAD
    /**
     * Tìm kiếm người dùng theo tên đăng nhập (email hoặc số điện thoại)
     * @param username
     * @return : UserDetails (username, password, roles)
     * @throws UsernameNotFoundException
     */
=======

>>>>>>> 70eb395 (create docker)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm theo email hoặc số điện thoại
        Account account = accountRepository.findByEmail(username)
                .or(() -> accountRepository.findByPhoneNumber(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or phone: " + username));

        // Lấy role hoặc set mặc định nếu null
        String role = account.getRole();
        if (role == null || role.isBlank()) {
            role = "USER";
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(account.getPassword())
<<<<<<< HEAD
                .roles(account.getRole())
                .build();
    }
=======
                .roles(role)
                .build();
    }


    private ResponseEntity<Map<String, Object>> buildErrorResponse(Map<String, String> errors, HttpStatus status) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("status", status.value());
        response.put("errors", errors);
        return ResponseEntity.status(status).body(response);
    }


>>>>>>> 70eb395 (create docker)
}
