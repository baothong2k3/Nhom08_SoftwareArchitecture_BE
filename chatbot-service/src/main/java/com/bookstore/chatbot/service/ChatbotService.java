package com.bookstore.chatbot.service;

import com.bookstore.chatbot.config.BookServiceClient;
import com.bookstore.chatbot.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final BookServiceClient bookServiceClient;
    
    public ChatMessage processMessage(String message) {
        message = message.toLowerCase();
        
        // Kiểm tra câu hỏi về sách
        if (message.contains("có sách") || message.contains("tìm sách")) {
            String bookTitle = extractBookTitle(message);
            if (bookTitle != null) {
                Object book = bookServiceClient.getBookByTitle(bookTitle);
                if (book != null) {
                    return new ChatMessage(message, "Vâng, chúng tôi có sách " + bookTitle, ChatMessage.MessageType.BOT);
                }
                return new ChatMessage(message, "Xin lỗi, chúng tôi không có sách " + bookTitle, ChatMessage.MessageType.BOT);
            }
        }
        
        // Kiểm tra câu hỏi về số lượng
        if (message.contains("còn bao nhiêu") || message.contains("số lượng")) {
            String bookTitle = extractBookTitle(message);
            if (bookTitle != null) {
                Object book = bookServiceClient.getBookByTitle(bookTitle);
                if (book != null) {
                    // Giả sử book object có trường id
                    Long bookId = extractBookId(book);
                    if (bookId != null) {
                        Object stock = bookServiceClient.getBookStock(bookId);
                        return new ChatMessage(message, "Sách " + bookTitle + " còn " + stock + " cuốn", ChatMessage.MessageType.BOT);
                    }
                }
                return new ChatMessage(message, "Xin lỗi, chúng tôi không có thông tin về số lượng sách " + bookTitle, ChatMessage.MessageType.BOT);
            }
        }
        
        return new ChatMessage(message, "Xin lỗi, tôi không hiểu câu hỏi của bạn. Bạn có thể hỏi về sách hoặc số lượng sách.", ChatMessage.MessageType.BOT);
    }
    
    private String extractBookTitle(String message) {
        // Pattern để tìm tên sách sau các từ khóa
        Pattern pattern = Pattern.compile("(?:có sách|tìm sách|còn bao nhiêu|số lượng)\\s+(.+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    private Long extractBookId(Object book) {
        // Implement logic để lấy ID từ book object
        // Đây là một ví dụ đơn giản, bạn cần điều chỉnh theo cấu trúc thực tế của book object
        try {
            return Long.parseLong(book.toString());
        } catch (Exception e) {
            return null;
        }
    }
} 