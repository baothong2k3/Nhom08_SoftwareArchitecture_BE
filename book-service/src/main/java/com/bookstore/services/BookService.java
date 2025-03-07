package com.bookstore.services;

import com.bookstore.entities.Book;

import java.util.List;

public interface BookService {
    public List<Book> getAllBooks();
    public Book getBookById(Long id);
    public Book saveBook(Book book);
    public Book updateBook(Long id, Book book);
    public void deleteBook(Long id);
}
