package com.bookstore.services.impl;

import com.bookstore.dtos.BookDTO;
import com.bookstore.entities.Book;
import com.bookstore.repositories.BookRepository;
import com.bookstore.services.BookService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Page<BookDTO> getAllBooksPaged(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Book> booksPage;
        if (keyword == null || keyword.trim().isEmpty()) {
            booksPage = bookRepository.findAll(pageable);
        } else {
            booksPage = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword, pageable);
        }

        return booksPage.map(book -> modelMapper.map(book, BookDTO.class));
    }

    @Override
    public Page<BookDTO> getBooksByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Book> booksPage = bookRepository.findByCategory(category, pageable);
        return booksPage.map(book -> modelMapper.map(book, BookDTO.class));
    }



    @Override
    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        return book != null ? modelMapper.map(book, BookDTO.class) : null;
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
        if(book.getDiscountPercent() != null && book.getDiscountPercent() > 0) {
           book.setDiscountPercent(book.getDiscountPercent());
        } else {
            book.setDiscountedPrice(book.getPrice());
        }
        return bookRepository.save(book);
    }

    @Override
    public BookDTO partialUpdateBook(Long id, BookDTO bookDTO) {
        return bookRepository.findById(id)
                .map(book -> {
                    // Only update fields that are provided in the DTO
                    if (bookDTO.getTitle() != null) {
                        book.setTitle(bookDTO.getTitle());
                    }
                    if (bookDTO.getAuthor() != null) {
                        book.setAuthor(bookDTO.getAuthor());
                    }
                    if (bookDTO.getPrice() != null) {
                        book.setPrice(bookDTO.getPrice());
                    }
                    if (bookDTO.getCategory() != null) {
                        book.setCategory(bookDTO.getCategory());
                    }
                    if (bookDTO.getDescription() != null) {
                        book.setDescription(bookDTO.getDescription());
                    }

                    // For primitive types, we need to check if they were explicitly included
                    // For stockQuantity, if it's included in the DTO (even if 0), update it
                    if (bookDTO.getStockQuantity() >= 0) {
                        book.setStockQuantity(bookDTO.getStockQuantity());
                    }

                    // For status, we assume any explicit value in the DTO should be used
                    book.setStatus(bookDTO.isStatus());

                    // Do not update imageUrl, publicId, or handle imageFile here

                    // updatedAt will be set by the @PreUpdate method

                    Book savedBook = bookRepository.save(book);
                    return modelMapper.map(savedBook, BookDTO.class);
                })
                .orElse(null);
    }

    @Override
    public BookDTO updateBookImage(Long id, MultipartFile imageFile) {
        return bookRepository.findById(id)
                .map(book -> {
                    try {
                        // Delete the existing image from Cloudinary if it exists
                        if (book.getPublicId() != null && !book.getPublicId().isEmpty()) {
                            cloudinary.uploader().destroy(book.getPublicId(), ObjectUtils.emptyMap());
                        }

                        // Upload the new image
                        var uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());

                        // Update book with new image information
                        book.setPublicId(uploadResult.get("public_id").toString());
                        book.setImageUrl(uploadResult.get("url").toString());

                        // The updatedAt field will be set by the @PreUpdate method

                        // Save the updated book
                        Book savedBook = bookRepository.save(book);
                        return modelMapper.map(savedBook, BookDTO.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to update book image", e);
                    }
                })
                .orElse(null);
    }

    @Override
    public List<BookDTO> getNewestBooks() {
        List<Book> books = bookRepository.findTop10ByOrderByCreatedAtDesc();
        return books.stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void updateStockQuantity(Long id, int quantity) {
        bookRepository.findById(id).ifPresent(book -> {
            if (book.getStockQuantity() >= quantity) {
                book.setStockQuantity(book.getStockQuantity() - quantity);
                bookRepository.save(book);
            } else {
                throw new IllegalArgumentException("Not enough stock for book ID: " + id);
            }
        });
    }

    @Override
    public void increaseStock(Long id, int quantity) {
        bookRepository.findById(id).ifPresent(book -> {
            book.setStockQuantity(book.getStockQuantity() + quantity);
            bookRepository.save(book);
        });
    }

    @Override
    public boolean isStockAvailable(Long bookId, int quantity) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            return book.getStockQuantity() >= quantity;
        }
        return false;
    }


    @Override
    public List<BookDTO> searchBooks(String keyword) {
        List<Book> books = bookRepository.searchBooksByTitleOrAuthor(keyword);
        return books.stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllCategories() {
        return bookRepository.findDistinctCategories();
    }


    @Transactional
    @Override
    public Book updateBookInfo(Long id, BookDTO bookDTO, MultipartFile imageFile) {
        Book existingBook = bookRepository.findById(id).orElse(null);
        if (existingBook == null) return null;

        existingBook.setAuthor(bookDTO.getAuthor());
        existingBook.setPrice(bookDTO.getPrice());
        existingBook.setStockQuantity(bookDTO.getStockQuantity());
        existingBook.setCategory(bookDTO.getCategory());
        existingBook.setDescription(bookDTO.getDescription());
        existingBook.setStatus(bookDTO.isStatus());
        existingBook.setDiscountPercent(bookDTO.getDiscountPercent());
        // setDiscountPercent sẽ tự động tính discountedPrice

        // Nếu có ảnh mới, xử lý upload và cập nhật
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Xoá ảnh cũ
                if (existingBook.getPublicId() != null && !existingBook.getPublicId().isEmpty()) {
                    cloudinary.uploader().destroy(existingBook.getPublicId(), ObjectUtils.emptyMap());
                }

                // Upload ảnh mới
                var uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());

                existingBook.setPublicId(uploadResult.get("public_id").toString());
                existingBook.setImageUrl(uploadResult.get("url").toString());
            } catch (IOException e) {
                throw new RuntimeException("Error handling image file", e);
            }
        }
        return bookRepository.save(existingBook);
    }


}
