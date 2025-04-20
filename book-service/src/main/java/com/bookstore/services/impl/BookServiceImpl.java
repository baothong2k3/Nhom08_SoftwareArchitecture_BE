package com.bookstore.services.impl;

import com.bookstore.entities.Book;
import com.bookstore.repositories.BookRepository;
import com.bookstore.services.BookService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @Override
    public Book saveBook(Book book, MultipartFile imageFile) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                var uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
                book.setPublicId(uploadResult.get("public_id").toString());
                book.setImageUrl(uploadResult.get("url").toString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image", e);
        }
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
