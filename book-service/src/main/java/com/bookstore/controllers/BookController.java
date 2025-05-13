package com.bookstore.controllers;
import com.bookstore.dtos.BookDTO;
import com.bookstore.entities.Book;
import com.bookstore.services.BookService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Book> saveBook(@ModelAttribute BookDTO bookDTO) {
        logger.info("Received save book request: {}", bookDTO.getTitle());
        logger.debug("DEBUG FILE: {}", (bookDTO.getImageFile() != null ? bookDTO.getImageFile().getOriginalFilename() : "null"));

        Book book = modelMapper.map(bookDTO, Book.class);
        Book savedBook = bookService.saveBook(book, bookDTO.getImageFile());
        return ResponseEntity.ok(savedBook);
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
}
