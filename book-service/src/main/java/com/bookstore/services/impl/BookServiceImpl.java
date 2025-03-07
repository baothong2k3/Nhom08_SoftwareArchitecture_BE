package com.bookstore.services.impl;

import com.bookstore.entities.Book;
import com.bookstore.repositories.BookRepository;
import com.bookstore.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/books")
public class BookServiceImpl implements BookService {
    @Autowired
    private BookRepository bookRepository;
    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public Book updateBook(Long id, Book book) {
        return bookRepository.findById(id)
                .map(b -> {
                    b.setTitle(book.getTitle());
                    b.setAuthor(book.getAuthor());
                    b.setPrice(book.getPrice());
                    b.setStockQuantity(book.getStockQuantity());
                    b.setCategory(book.getCategory());
                    b.setDescription(book.getDescription());
                    b.setCreatedAt(book.getCreatedAt());
                    return bookRepository.save(b);
                })
                .orElse(null);
    }

    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}
