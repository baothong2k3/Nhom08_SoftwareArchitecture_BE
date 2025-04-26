package com.bookstore.services;


import com.bookstore.dtos.BookDTO;

import java.util.List;

public interface BookService {
    public List<BookDTO> getAllBooks();
    public BookDTO getBookById(Long id);
    public BookDTO saveBook(BookDTO book);
    public BookDTO updateBook(Long id, BookDTO book);
    public void deleteBook(Long id);
}
