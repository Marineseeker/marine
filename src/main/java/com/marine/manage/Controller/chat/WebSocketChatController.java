package com.marine.manage.Controller.chat;

import com.marine.manage.pojo.chat.ChatMessage;
import com.marine.manage.service.chat.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final OnlineUserService onlineUserService;

    @MessageMapping("/chat.sendMessage")
    // SendTo注解将被注解的方法的返回值发送到指定的消息代理目的地
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        log.info("收到群聊消息 - 发送者: {}, 内容: {}",
                chatMessage.getSender(),
                chatMessage.getContent());
        return chatMessage;
    }

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        log.info("收到私聊消息 - 发送者: {}, 接收者: {}, 内容: {}",
                chatMessage.getSender(),
                chatMessage.getReceiver(),
                chatMessage.getContent());

        // convertAndSendToUser 是Spring WebSocket中用于向特定用户发送消息的核心方法
        /*
         * 原始调用：convertAndSendToUser("张三", "queue/messages", message)
         * 实际发送到：/user/张三/queue/messages
         */
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver(),
                "queue/messages",
                chatMessage);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        // StompHeaderAccessor 是Spring WebSocket中用于访问STOMP协议消息头和会话信息的核心工具类
        /*
         * getSessionId(): 获取WebSocket会话的唯一标识符
         * getSessionAttributes(): 获取会话属性Map，用于存储会话级别的数据
         * getUser(): 获取当前认证用户信息
         * getDestination(): 获取消息目的地
         * wrap(Message): 静态方法，包装Message对象为StompHeaderAccessor
         */
        Map<String,Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put("username", username);
        }

        String sessionId = headerAccessor.getSessionId();
        onlineUserService.userOnline(username, sessionId);

        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setContent(username + " 加入了聊天室");

        log.info("用户加入 - 用户名: {}, sessionId: {}", username, sessionId);
        return chatMessage;
    }

    @GetMapping("/chat/online-users")
    @ResponseBody
    public Map<String, String> getOnlineUsers() {
        return onlineUserService.getOnlineUsers();
    }

    @GetMapping("/chat/online-count")
    @ResponseBody
    public int getOnlineUserCount() {
        return onlineUserService.getOnlineUserCount();
    }
}
