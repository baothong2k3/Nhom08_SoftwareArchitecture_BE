package bookstore.authservice.services.impl;


import bookstore.authservice.entities.Account;
import bookstore.authservice.entities.Role;
import bookstore.authservice.repositories.AccountRepository;
import bookstore.authservice.repositories.RoleRepository;
import bookstore.authservice.services.AccountService;
import bookstore.authservice.dtos.ApiResponse;
import bookstore.authservice.dtos.SignUpRequest;
import bookstore.authservice.services.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AccountServiceImpl implements AccountService {


    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;

    @Autowired
    public AccountServiceImpl(
            AccountRepository accountRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ModelMapper modelMapper) {
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.modelMapper = modelMapper;
    }



    @Override
    public ResponseEntity<ApiResponse<?>> signUp(SignUpRequest signUpRequest) {
        // Kiểm tra username và email đã tồn tại chưa
        if (accountRepository.existsAccountByUserName(signUpRequest.getUserName())) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                            .status("ERROR")
                            .message("Username is already taken!")
                            .build());
        }
        // Kiểm tra email đã tồn tại chưa
        if (accountRepository.existsAccountByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Email is already taken!")
                    .build());
        }

        // Mã hóa mật khẩu trước khi lưu vào database
        String encryptPassword = passwordEncoder.encode(signUpRequest.getPassword());
        signUpRequest.setPassword(encryptPassword);

        // Chuyển SignUpRequest thành Account
        Account account = modelMapper.map(signUpRequest, Account.class);

        // Gán quyền USER cho tài khoản mới
        Role userRole = roleRepository.findRoleByName("USER");
        account.setRoles(Collections.singleton(userRole));


        accountRepository.save(account);

        String token = jwtService.generateToken(account.getUserName(), "USER");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .status("SUCCESS")
                        .message("Account created successfully")
                        .response(token)
                        .build());
    }

//    @Override
//    public ResponseEntity<ApiResponse<?>> signIn(SignInRequest signInRequest) {
//        return null;
//    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
