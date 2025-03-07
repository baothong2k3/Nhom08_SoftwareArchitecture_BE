package bookstore.authservice.services.impl;

import bookstore.authservice.entities.Role;
import bookstore.authservice.exceptions.UserAlreadyExistsException;
import bookstore.authservice.services.AuthService;
import bookstore.authservice.services.TokenService;
import bookstore.authservice.services.UserService;
import bookstore.authservice.auths.UserPrincipal;
import bookstore.authservice.dtos.ApiResponse;
import bookstore.authservice.dtos.SignInRequest;
import bookstore.authservice.dtos.SignInResponse;
import bookstore.authservice.dtos.SignUpRequest;
import bookstore.authservice.entities.Token;
import bookstore.authservice.entities.User;
import bookstore.authservice.services.RoleService;
import bookstore.authservice.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private UserService userService;
    private RoleService roleService;
    private TokenService tokenService;
    private PasswordEncoder passwordEncoder;
    private JwtTokenUtil jwtTokenUtil;
    private AuthenticationManager authenticationManager;
    private JwtEncoder jwtEncoder;

    @Autowired
    public AuthServiceImpl(UserService userService,
                           RoleService roleService,
                           TokenService tokenService,
                           JwtTokenUtil jwtTokenUtil,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtEncoder jwtEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.tokenService = tokenService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
    }

    @Override
    public ResponseEntity<ApiResponse<?>> signUp(SignUpRequest signUpRequest)
            throws UserAlreadyExistsException {

        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());



        String userServiceUrl = "http://localhost:8081/api/users/create";

//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<SignUpRequest> requestEntity = new HttpEntity<>(encodedRequest, headers);
//
//        ResponseEntity<ApiResponse> responseEntity = restTemplate.exchange(
//                userServiceUrl, HttpMethod.POST, requestEntity, ApiResponse.class);
//
//        return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());

    }



    @Override
    public ResponseEntity<ApiResponse<?>> signIn(SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getUserName(),
                        signInRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenUtil.generateToken(authentication, jwtEncoder);
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        User user = new User();
        user.setId(userDetails.getId());
        user.setUserName(userDetails.getUsername());

        Token token = Token.builder()
                .token(jwt)
                .user(user)
                .expiryDate(jwtTokenUtil.generateExpirationDate())
                .revoked(false)
                .build();
        tokenService.saveToken(token);

        SignInResponse signInResponse = SignInResponse.builder()
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .id(userDetails.getId())
                .token(jwt)
                .type("Bearer")
                .roles(userDetails.getAuthorities())
                .build();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("SUCCESS")
                        .message("Sign in successfull!")
                        .response(signInResponse)
                        .build()
        );
    }
}
