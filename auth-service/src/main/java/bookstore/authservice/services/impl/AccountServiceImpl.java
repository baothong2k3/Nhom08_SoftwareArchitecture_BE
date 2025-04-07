package bookstore.authservice.services.impl;


import bookstore.authservice.dtos.SignInRequest;
import bookstore.authservice.entities.Account;
import bookstore.authservice.repositories.AccountRepository;
import bookstore.authservice.services.AccountService;
import bookstore.authservice.dtos.SignUpRequest;
import bookstore.authservice.services.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;


    @Autowired
    public AccountServiceImpl(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ModelMapper modelMapper) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.modelMapper = modelMapper;
    }


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
    public ResponseEntity<?> signIn(SignInRequest signInRequest) {
        return null;
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return accountRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm theo email hoặc số điện thoại
        Account account = accountRepository.findByEmail(username)
                .or(() -> accountRepository.findByPhoneNumber(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or phone: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(account.getPhoneNumber())
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }
}
