package bookstore.userservice.exceptions;

import bookstore.userservice.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Xử lý lỗi tài khoản đã tồn tại
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.<String>builder()
                        .status("FAILED")
                        .message(ex.getMessage())
                        .build()
        );
    }

    // Xử lý lỗi Item không tìm thấy (404 NOT FOUND)
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleItemNotFoundException(ItemNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.<String>builder()
                        .status("FAILED")
                        .message(ex.getMessage())
                        .build()
        );
    }

    // Xử lý tất cả lỗi hệ thống chưa được bắt
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<String>builder()
                        .status("ERROR")
                        .message("An unexpected error occurred: " + ex.getMessage())
                        .build()
        );
    }
}
