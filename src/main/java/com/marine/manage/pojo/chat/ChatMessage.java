package com.marine.manage.pojo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private long id;
    private MessageType type;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
    private String roomId;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING
    }
}
