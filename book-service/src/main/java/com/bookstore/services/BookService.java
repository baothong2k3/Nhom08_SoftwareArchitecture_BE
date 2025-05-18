package com.bookstore.services;

import com.bookstore.dtos.BookDTO;
import com.bookstore.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    Page<BookDTO> getAllBooksPaged(int page, int size);
    public BookDTO getBookById(Long id);
    public Book saveBook(Book book, MultipartFile imageFile);
    public BookDTO partialUpdateBook(Long id, BookDTO bookDTO);
    public BookDTO updateBookImage(Long id, MultipartFile imageFile);
    List<BookDTO> getNewestBooks();
    void updateStockQuantity(Long id, int quantity);
    boolean isStockAvailable(Long bookId, int quantity);
    void increaseStock(Long id, int quantity);
    Book updateBookInfo(Long id, BookDTO bookDTO, MultipartFile imageFile);

}
