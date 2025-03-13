package bookstore.userservice.controllers;


import bookstore.userservice.dtos.AddressDTO;
import bookstore.userservice.dtos.ApiResponse;
import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.services.AddressService;
import bookstore.userservice.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User API", description = "Perform CRUD operations on users")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    @Autowired
    public UserController(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }


    @Operation(summary = "Save user", description = "Save a new user")
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<?>> registerUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        if (userService.existsByUserName(userDTO.getUserName())) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()  //400
                    .status("ERROR")
                    .message("Username already exists")
                    .build());
        }
        if (userService.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Email already exists")
                    .build());
        }

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("ERROR")
                    .message("Validation failed")
                    .response(errors)
                    .build());
        }


        UserDTO user = UserDTO.builder()
                .email(userDTO.getEmail())
                .userName(userDTO.getUserName())
                .enabled(userDTO.isEnabled())
                .phoneNumber(userDTO.getPhoneNumber())
                .dob(userDTO.getDob())
                .build();

        UserDTO uDTO  =  userService.save(user);


        if (userDTO.getAddresses() != null && !userDTO.getAddresses().isEmpty()) {
            for (var addressDTO : userDTO.getAddresses()) {
                AddressDTO add = AddressDTO.builder()
                        .address(addressDTO.getAddress())
                        .user(uDTO)
                        .build();
                addressService.save(add);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .status("SUCCESS")
                        .message("User created successfully")
                        .build());
    }


    @GetMapping("/{id}")
    @Operation(summary = "getUserById", description = "Get user by id")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.findById(id);
        ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
                .status("SUCCESS")
                .message("User fetched successfully")
                .response(userDTO)
                .build();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/all")
    @Operation(summary = "getAllUsers", description = "Get all users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        List<UserDTO> users = userService.findAll();
        ApiResponse<List<UserDTO>> response = ApiResponse.<List<UserDTO>>builder()
                .status("SUCCESS")
                .message("Get user list successfully")
                .response(users)
                .build();
        return ResponseEntity.ok(response);
    }
}
