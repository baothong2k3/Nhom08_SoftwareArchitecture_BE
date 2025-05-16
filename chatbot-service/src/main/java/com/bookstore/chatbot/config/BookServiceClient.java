package com.bookstore.chatbot.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "book-service")
public interface BookServiceClient {
    @GetMapping("/api/books/title/{title}")
    Object getBookByTitle(@PathVariable("title") String title);
    
    @GetMapping("/api/books/stock/{id}")
    Object getBookStock(@PathVariable("id") Long id);
} 