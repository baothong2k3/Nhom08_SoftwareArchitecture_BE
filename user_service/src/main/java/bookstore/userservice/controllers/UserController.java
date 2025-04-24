package bookstore.userservice.controllers;


import bookstore.userservice.dtos.AddressRequest;
import bookstore.userservice.dtos.ApiResponse;
import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.dtos.UserRequest;
import bookstore.userservice.entities.Address;
import bookstore.userservice.services.AddressService;
import bookstore.userservice.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody UserRequest userRequest, BindingResult bindingResult) {
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
            return ResponseEntity.badRequest().body(response);
        }
        // Kiểm tra xem số điện thoại đã tồn tại hay chưa
        if (userService.existsByPhoneNumber(userRequest.getPhoneNumber())) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", Map.of("phoneNumber", "Số điện thoại đã tồn tại trong hệ thống!"));
            return ResponseEntity.badRequest().body(response);
        }


        try {
            UserRequest savedUser = userService.save(userRequest);
            response.put("status", HttpStatus.CREATED.value());
            response.put("message", "User created successfully!");
            response.put("data", savedUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (DataIntegrityViolationException ex) { // Bắt lỗi trùng số điện thoại
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("errors", Map.of("phoneNumber", "Số điện thoại đã tồn tại trong hệ thống!"));
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Đã xảy ra lỗi không mong muốn");
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
    @PostMapping("/add-address")
    @Operation(summary = "Add address to user", description = "Add a new address for a user")
    public ResponseEntity<ApiResponse<Address>> addAddress(@Valid @RequestBody AddressRequest addressRequest) {
        try {
            Address savedAddress = addressService.addAddress(addressRequest);
            ApiResponse<Address> response = ApiResponse.<Address>builder()
                    .status("SUCCESS")
                    .message("Address added successfully")
                    .response(savedAddress)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            ApiResponse<Address> response = ApiResponse.<Address>builder()
                    .status("FAILURE")
                    .message(ex.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            ApiResponse<Address> response = ApiResponse.<Address>builder()
                    .status("ERROR")
                    .message("An unexpected error occurred")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
