package com.marine.manage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marine.manage.pojo.ChatSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 对话会话管理服务
 * 负责管理用户的对话上下文
 */
@Slf4j
@Service
public class ChatSessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 单个会话最大消息数量
    private static final int MAX_MESSAGES_PER_SESSION = 50;

    private static final String CHAT_SESSION_KEY_PREFIX = "chat_session:";
    private static final String USER_SESSIONS_KEY_PREFIX = "user_sessions:";
    // 会话超时时间（分钟）
    private static final long SESSION_TIMEOUT_MINUTES = 30;

    /**
     * 创建新会话
     */
    public String createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        String redisKey = CHAT_SESSION_KEY_PREFIX + sessionId;
        String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;

        ChatSession session = ChatSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title("新对话")
                .createTime(LocalDateTime.now())
                .lastActiveTime(LocalDateTime.now())
                .messages(new ArrayList<>())
                .build();

        // 存储会话信息
        redisTemplate.opsForValue().set(redisKey,
                                        session, 
                                        SESSION_TIMEOUT_MINUTES, 
                                        java.util.concurrent.TimeUnit.MINUTES);

        // 将会话ID添加到用户的会话集合中
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        redisTemplate.expire(userSessionsKey, SESSION_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);

        log.info("创建新会话: {} for user: {}", sessionId, userId);
        return sessionId;
    }

    /**
     * 获取会话
     */
    public ChatSession getSession(String sessionId) {
        String redisKey = CHAT_SESSION_KEY_PREFIX + sessionId;
        Object obj = redisTemplate.opsForValue().get(redisKey);
        ChatSession session = null;

        if (obj instanceof ChatSession) {
            session = (ChatSession) obj;
        } else if (obj instanceof Map) {
            // 反序列化为 ChatSession
            session = objectMapper.convertValue(obj, ChatSession.class);
        }

        if (session != null) {
            // 检查会话是否超时
            if (isSessionExpired(session)) {
                deleteSessionFromRedis(sessionId, session.getUserId());
                log.info("会话已超时，自动删除: {}", sessionId);
                return null;
            }
            // 更新最后活跃时间
            session.setLastActiveTime(LocalDateTime.now());
            // 重新保存到Redis以更新过期时间
            redisTemplate.opsForValue().set(redisKey, session, SESSION_TIMEOUT_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
        }
        return session;
    }

    /**
     * 添加消息到会话
     */
    public void addMessage(String sessionId, String role, String content, String messageType) {
        ChatSession session = getSession(sessionId);
        if (session == null) {
            log.warn("会话不存在: {}", sessionId);
            return;
        }

        ChatSession.ChatMessage message = ChatSession.ChatMessage.builder()
                .role(role)
                .content(content)
                .timestamp(LocalDateTime.now())
                .messageType(messageType != null ? messageType : "text")
                .build();

        // 限制消息数量，删除最老的消息
        if (session.getMessages().size() >= MAX_MESSAGES_PER_SESSION) {
            session.getMessages().remove(0);
        }

        // 在这里添加了新的上下文到session
        session.getMessages().add(message);
        session.setLastActiveTime(LocalDateTime.now());

        // 自动生成会话标题
        if (session.getMessages().size() == 1 && "user".equals(role)) {
            String title = content.length() > 20 ? content.substring(0, 20) + "..." : content;
            session.setTitle(title);
        }

        // 保存更新后的会话到 Redis
        String redisKey = CHAT_SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(redisKey, 
                                        session, 
                                        SESSION_TIMEOUT_MINUTES, 
                                        java.util.concurrent.TimeUnit.MINUTES);

        log.debug("添加消息到会话 {}: {} - {}", sessionId, role, content.substring(0, Math.min(50, content.length())));
    }

    /**
     * 构建上下文消息列表
     * 用于传递给AI模型
     */
    public List<Map<String, String>> buildContextMessages(String sessionId, int maxMessages) {
        ChatSession session = getSession(sessionId);
        if (session == null || session.getMessages().isEmpty()) {
            return new ArrayList<>();
        }

        List<ChatSession.ChatMessage> messages = session.getMessages();
        int startIndex = Math.max(0, messages.size() - maxMessages);

        List<Map<String, String>> contextMessages = new ArrayList<>();
        for (int i = startIndex; i < messages.size(); i++) {
            ChatSession.ChatMessage msg = messages.get(i);
            Map<String, String> contextMsg = new HashMap<>();
            contextMsg.put("role", msg.getRole());
            contextMsg.put("content", msg.getContent());
            contextMessages.add(contextMsg);
        }

        return contextMessages;
    }

    /**
     * 获取用户的所有会话
     */
    public List<ChatSession> getUserSessions(String userId) {
        String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;
        Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

        if (sessionIds == null || sessionIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ChatSession> userSessions = new ArrayList<>();
        for (Object sessionIdObj : sessionIds) {
            String sessionId = sessionIdObj.toString();
            ChatSession session = getSession(sessionId);
            if (session != null && !isSessionExpired(session)) {
                userSessions.add(session);
            } else {
                // 清理过期的会话ID
                redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
            }
        }

        // 按最后活跃时间排序
        userSessions.sort((s1, s2) -> s2.getLastActiveTime().compareTo(s1.getLastActiveTime()));
        return userSessions;
    }

    /**
     * 删除会话
     */
    public boolean deleteSession(String sessionId, String userId) {
        ChatSession session = getSession(sessionId);
        if (session != null && userId.equals(session.getUserId())) {
            deleteSessionFromRedis(sessionId, userId);
            log.info("删除会话: {}", sessionId);
            return true;
        }
        return false;
    }

    /**
     * 从Redis中删除会话
     */
    private void deleteSessionFromRedis(String sessionId, String userId) {
        String redisKey = CHAT_SESSION_KEY_PREFIX + sessionId;
        String userSessionsKey = USER_SESSIONS_KEY_PREFIX + userId;

        // 删除会话数据
        redisTemplate.delete(redisKey);
        // 从用户会话集合中移除
        redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
    }

    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        // 获取所有用户会话键
        Set<String> userSessionKeys = redisTemplate.keys(USER_SESSIONS_KEY_PREFIX + "*");
        int removedCount = 0;

        if (userSessionKeys != null) {
            for (String userSessionKey : userSessionKeys) {
                Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionKey);
                if (sessionIds != null) {
                    for (Object sessionIdObj : sessionIds) {
                        String sessionId = sessionIdObj.toString();
                        String sessionKey = CHAT_SESSION_KEY_PREFIX + sessionId;

                        // 检查会话是否存在
                        if (!redisTemplate.hasKey(sessionKey)) {
                            // 会话已过期，从用户会话集合中移除
                            redisTemplate.opsForSet().remove(userSessionKey, sessionId);
                            removedCount++;
                        }
                    }
                }
            }
        }

        if (removedCount > 0) {
            log.info("清理过期会话: {} 个", removedCount);
        }
    }

    /**
     * 检查会话是否过期
     */
    private boolean isSessionExpired(ChatSession session) {
        LocalDateTime expireTime = session.getLastActiveTime().plusMinutes(SESSION_TIMEOUT_MINUTES);
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 获取会话统计信息
     */
    public Map<String, Object> getSessionStats() {
        // 获取所有会话键的数量
        Set<String> sessionKeys = redisTemplate.keys(CHAT_SESSION_KEY_PREFIX + "*");
        int totalSessions = sessionKeys != null ? sessionKeys.size() : 0;

        // 计算活跃会话数量
        int activeSessions = 0;
        if (sessionKeys != null) {
            for (String sessionKey : sessionKeys) {
                Object obj = redisTemplate.opsForValue().get(sessionKey);
                if (obj != null) {
                    ChatSession session;
                    if (obj instanceof ChatSession) {
                        session = (ChatSession) obj;
                    } else if (obj instanceof Map) {
                        session = objectMapper.convertValue(obj, ChatSession.class);
                    } else {
                        continue;
                    }

                    if (!isSessionExpired(session)) {
                        activeSessions++;
                    }
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", totalSessions);
        stats.put("activeSessions", activeSessions);
        return stats;
    }
}
