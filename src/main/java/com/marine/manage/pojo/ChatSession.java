package com.marine.manage.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话会话实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    private String sessionId;
    private String userId;
    private String title;
    private LocalDateTime createTime;
    private LocalDateTime lastActiveTime;
    private List<ChatMessage> messages;

    /**
     * 对话消息实体类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessage {
        private String role; // "user" 或 "assistant"
        private String content;
        private LocalDateTime timestamp;
        private String messageType; // "text", "knowledge_based" 等
    }
}
