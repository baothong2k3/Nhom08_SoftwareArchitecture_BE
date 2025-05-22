package com.ai.gemini_chat.controller;

import com.ai.gemini_chat.models.ChatMessage;
import com.ai.gemini_chat.service.QnAService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;



@RestController
@AllArgsConstructor
@RequestMapping("/api/qna")
public class AIController {
    private static final Logger log = LoggerFactory.getLogger(AIController.class);
    private final QnAService qnAService;
    private final ReactiveRedisTemplate<String, ChatMessage> redisTemplate;
    private static final long TTL_SECONDS = 24 * 60 * 60; // 24 giờ


    @PostMapping("/ask")
    public Mono<ResponseEntity<String>> askQuestion(@RequestBody Map<String, String> payload,
                                                    @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        System.out.println("Payload: " + payload);
        System.out.println("Session ID: " + sessionId);
        String question = payload.get("question");
        if (sessionId == null || sessionId.isEmpty()) {
            log.warn("No sessionId provided for /api/qna/ask");
            return Mono.just(ResponseEntity.badRequest().body("Missing sessionId"));
        }

        String redisKey = "chat:history:" + sessionId;
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // Lưu tin nhắn gửi
        ChatMessage sentMessage = new ChatMessage();
        sentMessage.setContent(question);
        sentMessage.setType("sent");
        sentMessage.setTime(time);

        return Mono.just(qnAService.getAnswer(question))
                .flatMap(answer -> {
                    // Lưu tin nhắn nhận
                    ChatMessage receivedMessage = new ChatMessage();
                    receivedMessage.setContent(answer);
                    receivedMessage.setType("received");
                    receivedMessage.setTime(time);

                    return redisTemplate.opsForList().rightPush(redisKey, sentMessage)
                            .then(redisTemplate.opsForList().rightPush(redisKey, receivedMessage))
                            .then(redisTemplate.expire(redisKey, java.time.Duration.ofSeconds(TTL_SECONDS)))
                            .thenReturn(ResponseEntity.ok(answer));
                })
                .doOnError(e -> log.error("Error processing /api/qna/ask: {}", e.getMessage()));
    }

    @GetMapping("/history")
    public Flux<ChatMessage> getHistory(@RequestHeader("X-Session-Id") String sessionId) {
        String redisKey = "chat:history:" + sessionId;
        log.info("Fetching chat history for sessionId: {}", sessionId);
        return redisTemplate.opsForList().range(redisKey, 0, -1)
                .cast(ChatMessage.class)
                .doOnError(e -> log.error("Error fetching history: {}", e.getMessage()));
    }

    @PostMapping("/clear")
    public Mono<ResponseEntity<String>> clearHistory(@RequestHeader("X-Session-Id") String sessionId) {
        String redisKey = "chat:history:" + sessionId;
        log.info("Clearing chat history for sessionId: {}", sessionId);
        return redisTemplate.delete(redisKey)
                .then(Mono.just(ResponseEntity.ok("Chat history cleared")))
                .doOnError(e -> log.error("Error clearing history: {}", e.getMessage()));
    }
}
