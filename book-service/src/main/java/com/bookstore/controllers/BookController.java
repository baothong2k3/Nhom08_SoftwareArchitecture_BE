package com.bookstore.controllers;
import com.bookstore.dtos.BookDTO;
import com.bookstore.entities.Book;
import com.bookstore.services.BookService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/paged")
    public ResponseEntity<Page<BookDTO>> getAllBooksPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookDTO> booksPage = bookService.getAllBooksPaged(page, size);
        return ResponseEntity.ok(booksPage);
    }

    @GetMapping("/newest")
    public ResponseEntity<List<BookDTO>> getNewestBooks() {
        List<BookDTO> newestBooks = bookService.getNewestBooks();
        return ResponseEntity.ok(newestBooks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        if (book != null) {
            return ResponseEntity.ok(book);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Không tìm thấy sách với ID = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping(value = "/save",consumes = {"multipart/form-data"})
    public ResponseEntity<?> saveBook(@Valid @ModelAttribute BookDTO bookDTO, BindingResult bindingResult) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());

            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", errorMessages);
            return ResponseEntity.badRequest().body(response);
        }
        try {
            Book book = modelMapper.map(bookDTO, Book.class);
            Book savedBook = bookService.saveBook(book, bookDTO.getImageFile());

            response.put("status", HttpStatus.OK.value());
            response.put("message", "Lưu sách thành công");
            response.put("data", savedBook);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi lưu sách");
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookDTO> partialUpdateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        logger.info("Partially updating book with ID: {}", id);
        BookDTO updatedBook = bookService.partialUpdateBook(id, bookDTO);
        if (updatedBook != null) {
            return ResponseEntity.ok(updatedBook);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/update-image")
    public ResponseEntity<BookDTO> updateBookImage(@PathVariable Long id, @RequestParam("imageFile") MultipartFile imageFile) {
        logger.info("Updating image for book with ID: {}", id);
        if (imageFile == null || imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        BookDTO updatedBook = bookService.updateBookImage(id, imageFile);
        if (updatedBook != null) {
            return ResponseEntity.ok(updatedBook);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/update-stock")
    public ResponseEntity<Void> updateStockQuantity(@PathVariable Long id, @RequestBody int quantity) {
        bookService.updateStockQuantity(id, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Boolean> checkStockAvailability(@PathVariable Long id, @RequestParam int requestedQuantity) {
        try {
            boolean isAvailable = bookService.isStockAvailable(id, requestedQuantity);
            return ResponseEntity.ok(isAvailable);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }


    @PatchMapping("/{id}/increase-stock")
    public ResponseEntity<Void> increase(@PathVariable Long id, @RequestBody int quantity) {
        bookService.increaseStock(id, quantity);
        return ResponseEntity.ok().build();
    }


    @PatchMapping(value = "/{id}/update", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateBookInfo(
            @PathVariable Long id,
            @ModelAttribute BookDTO bookDTO) {
        logger.info("Updating book with ID: {}", id);
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Book updatedBook = bookService.updateBookInfo(id, bookDTO, bookDTO.getImageFile());
            if (updatedBook != null) {
                response.put("status", HttpStatus.OK.value());
                response.put("message", "Cập nhật sách thành công");
                response.put("data", modelMapper.map(updatedBook, BookDTO.class));
                return ResponseEntity.ok(response);
            } else {
                response.put("status", HttpStatus.NOT_FOUND.value());
                response.put("message", "Không tìm thấy sách với ID = " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error updating book: {}", e.getMessage(), e);
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Lỗi khi cập nhật sách");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
  
     @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam String keyword) {
        List<BookDTO> books = bookService.searchBooks(keyword);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = bookService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

}
