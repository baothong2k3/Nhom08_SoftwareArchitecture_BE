package com.bookstore.controllers;
import com.bookstore.dtos.BookDTO;
import com.bookstore.entities.Book;
import com.bookstore.services.BookService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        BookDTO book = bookService.getBookById(id);
        if (book != null) {
            return ResponseEntity.ok(book);
        } else {
            return ResponseEntity.notFound().build();
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
}
