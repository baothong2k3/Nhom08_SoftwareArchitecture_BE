package com.bookstore.services;

import com.bookstore.entities.Book;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    public List<Book> getAllBooks();
    public Book getBookById(Long id);
    public Book saveBook(Book book, MultipartFile imageFile);
    public Book updateBook(Long id, Book book);
    public void deleteBook(Long id);
}