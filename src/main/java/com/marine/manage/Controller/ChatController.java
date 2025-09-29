package com.marine.manage.Controller;

import cn.dev33.satoken.stp.StpUtil;
import com.marine.manage.pojo.ChatSession;
import com.marine.manage.pojo.Result;
import com.marine.manage.service.ChatSessionService;
import com.marine.manage.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI聊天控制器
 * 提供AI对话相关的接口服务，支持上下文对话
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;
    private final KnowledgeBaseService knowledgeBaseService;
    private final ChatSessionService chatSessionService;

    /**
     * 简单对话接口
     * @param message 用户输入的消息
     * @return AI回复
     */
    @PostMapping("/simple")
    public Result<String> simpleChat(@RequestParam String message) {
        try {
            log.info("收到用户消息: {}", message);
            
            String response = chatClient.prompt()
                    .user(message)
                    .call()
                    .content();
            
            log.info("AI回复: {}", response);
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("AI对话发生错误", e);
            return Result.error("AI服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 带上下文的对话接口
     * @param request 包含消息和会话信息的请求对象
     * @return AI回复
     */
    @PostMapping("/context")
    public Result<Map<String, Object>> contextChat(@RequestBody ContextChatRequest request) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            log.info("收到上下文对话请求: {} from user: {}", request.getMessage(), userId);

            // 获取或创建会话
            String sessionId = request.getSessionId();
            if (sessionId == null || chatSessionService.getSession(sessionId) == null) {
                sessionId = chatSessionService.createSession(userId);
            }

            // 添加用户消息到会话历史
            chatSessionService.addMessage(sessionId, "user", request.getMessage(), "text");

            // 构建上下文消息（取最近10条消息）
            List<Map<String, String>> contextMessages = chatSessionService.buildContextMessages(sessionId, 10);

            // 构建提示词
            String systemPrompt = request.getUseKnowledge() != null && request.getUseKnowledge()
                ? knowledgeBaseService.buildSystemPromptWithKnowledge(request.getMessage())
                : getDefaultSystemPrompt();

            String response;

            if (contextMessages.size() <= 1) {
                // 首次对话或只有当前消息，直接调用
                response = chatClient.prompt()
                        .system(systemPrompt)
                        .user(request.getMessage())
                        .call()
                        .content();
            } else {
                // 有历史消息，构建完整的上下文提示词
                StringBuilder contextPrompt = new StringBuilder(systemPrompt);
                contextPrompt.append("\n\n以下是对话历史：\n");

                // 添加历史消息（除了最后一条用户消息）
                for (int i = 0; i < contextMessages.size() - 1; i++) {
                    Map<String, String> msg = contextMessages.get(i);
                    if ("user".equals(msg.get("role"))) {
                        contextPrompt.append("用户: ").append(msg.get("content")).append("\n");
                    } else {
                        contextPrompt.append("助手: ").append(msg.get("content")).append("\n");
                    }
                }

                contextPrompt.append("\n请基于以上对话历史，回答用户的新问题。");

                response = chatClient.prompt()
                        .system(contextPrompt.toString())
                        .user(request.getMessage())
                        .call()
                        .content();
            }

            // 添加AI回复到会话历史
            String messageType = request.getUseKnowledge() != null && request.getUseKnowledge()
                ? "knowledge_based" : "text";
            chatSessionService.addMessage(sessionId, "assistant", response, messageType);

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("sessionId", sessionId);
            result.put("messageCount", contextMessages.size() + 1);

            return Result.success(result);

        } catch (Exception e) {
            log.error("上下文对话发生错误", e);
            return Result.error("AI服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 检查AI服务状态
     * @return 服务状态
     */
    @GetMapping("/health")
    public Result<String> checkHealth() {
        try {
            String response = chatClient.prompt()
                    .user("hello")
                    .call()
                    .content();
            
            return Result.success("AI服务正常");
            
        } catch (Exception e) {
            log.error("AI服务健康检查失败", e);
            return Result.error("AI服务不可用");
        }
    }

    /**
     * 基于知识库的智能对话接口（支持上下文）
     * @param request 包含消息和会话信息的请求对象
     * @return AI回复
     */
    @PostMapping("/knowledge")
    public Result<Map<String, Object>> knowledgeBasedChat(@RequestBody KnowledgeChatRequest request) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            log.info("收到知识库对话请求: {} from user: {}", request.getMessage(), userId);

            // 获取或创建会话
            String sessionId = request.getSessionId();
            if (sessionId == null || chatSessionService.getSession(sessionId) == null) {
                sessionId = chatSessionService.createSession(userId);
            }

            // 添加用户消息到会话历史
            chatSessionService.addMessage(sessionId, "user", request.getMessage(), "knowledge_query");

            // 基于用户消息构建包含知识库信息的系统提示词
            String systemPrompt = knowledgeBaseService.buildSystemPromptWithKnowledge(request.getMessage());

            String response;

            if (request.getUseContext() != null && request.getUseContext()) {
                // 使用上下文模式
                List<Map<String, String>> contextMessages = chatSessionService.buildContextMessages(sessionId, 8);

                if (contextMessages.size() <= 1) {
                    // 首次对话，直接调用
                    response = chatClient.prompt()
                            .system(systemPrompt)
                            .user(request.getMessage())
                            .call()
                            .content();
                } else {
                    // 有历史消息，构建完整的上下文提示词
                    StringBuilder contextPrompt = new StringBuilder(systemPrompt);
                    contextPrompt.append("\n\n以下是对话历史：\n");

                    // 添加历史消息（除了最后一条用户消息）
                    for (int i = 0; i < contextMessages.size() - 1; i++) {
                        Map<String, String> msg = contextMessages.get(i);
                        if ("user".equals(msg.get("role"))) {
                            contextPrompt.append("用户: ").append(msg.get("content")).append("\n");
                        } else {
                            contextPrompt.append("助手: ").append(msg.get("content")).append("\n");
                        }
                    }

                    contextPrompt.append("\n请基于以上对话历史和知识库信息，回答用户的新问题。");

                    response = chatClient.prompt()
                            .system(contextPrompt.toString())
                            .user(request.getMessage())
                            .call()
                            .content();
                }
            } else {
                // 单轮对话模式（原有逻辑）
                response = chatClient.prompt()
                        .system(systemPrompt)
                        .user(request.getMessage())
                        .call()
                        .content();
            }

            // 添加AI回复到会话历史
            chatSessionService.addMessage(sessionId, "assistant", response, "knowledge_based");

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("sessionId", sessionId);
            result.put("useContext", request.getUseContext() != null && request.getUseContext());

            log.info("知识库AI回复: {}", response);
            return Result.success(result);

        } catch (Exception e) {
            log.error("知识库对话发生错误", e);
            return Result.error("AI服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 基于知识库的流式对话接口（支持上下文）
     */
    @PostMapping("/knowledge-stream")
    public SseEmitter knowledgeBasedStreamChat(@RequestBody KnowledgeChatRequest request) {
        // 手动验证登录状态
        if (!StpUtil.isLogin()) {
            throw new IllegalStateException("未登录，无法访问");
        }

        String userId = StpUtil.getLoginIdAsString();
        SseEmitter emitter = new SseEmitter(0L);

        log.info("收到知识库流式请求: {} from user: {}", request.getMessage(), userId);

        try {
            // 获取或创建会话
            final String sessionId = request.getSessionId() != null && chatSessionService.getSession(request.getSessionId()) != null
                ? request.getSessionId()
                : chatSessionService.createSession(userId);

            // 发送会话ID给客户端
            emitter.send(SseEmitter.event()
                    .name("session")
                    .data(Map.of("sessionId", sessionId)));

            // 添加用户消息到会话历史
            chatSessionService.addMessage(sessionId, "user", request.getMessage(), "knowledge_stream");

            // 基于用户消息构建包含知识库信息的系统提示词
            String systemPrompt = knowledgeBaseService.buildSystemPromptWithKnowledge(request.getMessage());

            Flux<String> flux;

            if (request.getUseContext() != null && request.getUseContext()) {
                // 使用上下文模式
                List<Map<String, String>> contextMessages = chatSessionService.buildContextMessages(sessionId, 8);

                if (contextMessages.size() <= 1) {
                    // 首次对话，直接调用
                    flux = chatClient.prompt()
                            .system(systemPrompt)
                            .user(request.getMessage())
                            .stream()
                            .content();
                } else {
                    // 有历史消息，构建完整的上下文提示词
                    StringBuilder contextPrompt = new StringBuilder(systemPrompt);
                    contextPrompt.append("\n\n以下是对话历史：\n");

                    // 添加历史消息（除了最后一条用户消息）
                    for (int i = 0; i < contextMessages.size() - 1; i++) {
                        Map<String, String> msg = contextMessages.get(i);
                        if ("user".equals(msg.get("role"))) {
                            contextPrompt.append("用户: ").append(msg.get("content")).append("\n");
                        } else {
                            contextPrompt.append("助手: ").append(msg.get("content")).append("\n");
                        }
                    }

                    contextPrompt.append("\n请基于以上对话历史和知识库信息，回答用户的新问题。");

                    flux = chatClient.prompt()
                            .system(contextPrompt.toString())
                            .user(request.getMessage())
                            .stream()
                            .content();
                }
            } else {
                // 单轮对话模式
                flux = chatClient.prompt()
                        .system(systemPrompt)
                        .user(request.getMessage())
                        .stream()
                        .content();
            }

            // 使用线程安全的StringBuilder作为final变量
            final StringBuilder responseBuilder = new StringBuilder();

            flux.subscribe(
                    contentChunk -> {
                        try {
                            synchronized (responseBuilder) {
                                responseBuilder.append(contentChunk);
                            }
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(contentChunk));
                        } catch (Exception e) {
                            log.error("发送 SSE 消息失败", e);
                            emitter.completeWithError(e);
                        }

                    },
                    error -> {
                        log.error("知识库流式对话发生错误", error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("AI服务暂时不可用，请稍后重试"));
                        } catch (Exception ignored) {}
                        emitter.completeWithError(error);
                    },
                    () -> {
                        try {
                            // 添加完整的AI回复到会话历史
                            chatSessionService.addMessage(sessionId, "assistant", responseBuilder.toString(), "knowledge_stream");

                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data(Map.of("message", "[DONE]", "sessionId", sessionId)));
                        } catch (Exception ignored) {}
                        emitter.complete();
                    }
            );
        } catch (Exception e) {
            log.error("构建知识库提示词失败", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("知识库服务暂时不可用"));
            } catch (Exception ignored) {}
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatSession.ChatMessage>> getSessionMessages(@PathVariable String sessionId) {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }

        ChatSession session = chatSessionService.getSession(sessionId);
        if (session == null) {
            return Result.error("会话不存在或已过期");
        }

        String userId = StpUtil.getLoginIdAsString();
        if (!userId.equals(session.getUserId())) {
            return Result.error("无权访问此会话");
        }

        return Result.success(session.getMessages());
    }

    /**
     * 获取用户所有会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> getUserSessions() {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }

        String userId = StpUtil.getLoginIdAsString();
        List<ChatSession> sessions = chatSessionService.getUserSessions(userId);
        return Result.success(sessions);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<String> deleteSession(@PathVariable String sessionId) {
        if (!StpUtil.isLogin()) {
            return Result.error("请先登录");
        }

        String userId = StpUtil.getLoginIdAsString();
        boolean deleted = chatSessionService.deleteSession(sessionId, userId);

        if (deleted) {
            return Result.success("会话删除成功");
        } else {
            return Result.error("会话删除失败");
        }
    }

    private String getDefaultSystemPrompt() {
        return """
            你是一个高校教务系统网站的智能助手。
            你的目标是帮助师生快速解决与教务相关的问题，提供简洁、准确的中文回答。
            """;
    }

    /**
     * 上下文聊天请求对象
     */
    public static class ContextChatRequest {
        private String message;
        private String sessionId;
        private Boolean useKnowledge; // 是否使用知识库

        // getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Boolean getUseKnowledge() { return useKnowledge; }
        public void setUseKnowledge(Boolean useKnowledge) { this.useKnowledge = useKnowledge; }
    }

    /**
     * 知识库聊天请求对象
     */
    public static class KnowledgeChatRequest {
        private String message;
        private String sessionId;
        private Boolean useContext; // 是否使用上下文

        // getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public Boolean getUseContext() { return useContext; }
        public void setUseContext(Boolean useContext) { this.useContext = useContext; }
    }
}
