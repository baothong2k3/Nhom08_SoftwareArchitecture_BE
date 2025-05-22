package com.ai.gemini_chat.models;

import lombok.Data;

@Data
public class ChatMessage {
    private String content;
    private String type; // "sent" or "received"
    private String time;
}
