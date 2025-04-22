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

    @Query("SELECT b FROM Book b ORDER BY function('RAND')")
    Page<Book> findAll(Pageable pageable);
}
