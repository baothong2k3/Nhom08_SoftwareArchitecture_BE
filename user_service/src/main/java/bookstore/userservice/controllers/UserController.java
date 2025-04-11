package bookstore.userservice.controllers;

import bookstore.userservice.dtos.ApiResponse;
import bookstore.userservice.dtos.UserDTO;
import bookstore.userservice.dtos.UserRequest;
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
        } catch (DataIntegrityViolationException ex) {
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

    @Operation(summary = "Get current user", description = "Lấy thông tin người dùng hiện tại từ JWT token")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        UserDTO userDTO = userService.getUserFromToken(authHeader);
        ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
                .status("SUCCESS")
                .message("Lấy thông tin người dùng thành công")
                .response(userDTO)
                .build();
        return ResponseEntity.ok(response);
    }
}
