package com.bookstore.services;

import com.bookstore.dtos.BookDTO;
import com.bookstore.entities.Book;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    public List<BookDTO> getAllBooks();
    public BookDTO getBookById(Long id);
    public Book saveBook(Book book, MultipartFile imageFile);
    public BookDTO partialUpdateBook(Long id, BookDTO bookDTO);
    public BookDTO updateBookImage(Long id, MultipartFile imageFile);
}
