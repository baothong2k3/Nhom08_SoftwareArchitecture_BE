package bookstore.authservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
//    @ExceptionHandler(value = MethodArgumentNotValidException.class)
//    public ResponseEntity<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException exception) {
//
//        List<String> errorMessage = new ArrayList<>();
//
//        exception.getBindingResult().getFieldErrors().forEach(error -> {
//            errorMessage.add(error.getDefaultMessage());
//        });
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(
//                        ApiResponse.builder()
//                                .status("FAILED")
//                                .message("Validation error")
//                                .errors(errorMessage)
//                                .build()
//                );
//    }
//
//    @ExceptionHandler(value = UserAlreadyExistsException.class)
//    public ResponseEntity<?> userAlreadyExistsExceptionHandler(UserAlreadyExistsException exception) {
//        return ResponseEntity
//                .status(HttpStatus.CONFLICT)
//                .body(
//                        ApiResponse.builder()
//                                .status("FAILED")
//                                .message(exception.getMessage())
//                                .build()
//                );
//    }
}
