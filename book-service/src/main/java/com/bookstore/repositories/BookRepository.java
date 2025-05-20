package com.bookstore.repositories;

import com.bookstore.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT b FROM Book b ORDER BY b.createdAt DESC")
    List<Book> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> searchBooksByTitleOrAuthor(String keyword);

    @Query("SELECT DISTINCT b.category FROM Book b")
    List<String> findDistinctCategories();

    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author, Pageable pageable);

    Page<Book> findByCategory(String category, Pageable pageable);

}
