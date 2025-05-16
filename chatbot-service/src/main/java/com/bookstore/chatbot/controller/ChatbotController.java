package com.bookstore.chatbot.controller;

import com.bookstore.chatbot.model.ChatMessage;
import com.bookstore.chatbot.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotController {
    private final ChatbotService chatbotService;
    
    @PostMapping("/message")
    public ChatMessage processMessage(@RequestBody ChatMessage message) {
        return chatbotService.processMessage(message.getMessage());
    }
} 