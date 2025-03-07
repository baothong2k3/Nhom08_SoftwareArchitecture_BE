package bookstore.userservice.controllers;


import bookstore.userservice.dtos.AddressDTO;
import bookstore.userservice.dtos.ApiResponse;
import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.entities.Address;
import bookstore.userservice.entities.User;
import bookstore.userservice.services.AddressService;
import bookstore.userservice.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RepositoryRestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    @Autowired
    public UserController(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }


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
                .password(userDTO.getPassword())
                .enabled(userDTO.isEnabled())
                .role(userDTO.getRole())
                .phoneNumber(userDTO.getPhoneNumber())
                .dob(userDTO.getDob())
                .addresses(userDTO.getAddresses())
                .build();

        Address address = Address.builder()
                        .address(userDTO.getAddresses().get(0).getAddress())
                        .build();


        addressService.save(address);
        userService.save(user);


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.builder()
                        .status("SUCCESS")
                        .message("User created successfully")
                        .build());
    }

}
