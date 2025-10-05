package com.marine.manage.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * WebSocket通道拦截器
 * 用于在Socket.connect时从会话属性中提取用户名, 转化为Principal, 并设置到消息头中
 */
@Slf4j
@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    @Nullable
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从WebSocket会话属性中获取用户名
            String username = accessor.getFirstNativeHeader("username");
            if(username != null) {
                Principal userPrincipal = new WebSocketUserPrincipal(username);
                accessor.setUser(userPrincipal);
                log.info("从STOMP headers设置WebSocket用户身份: {}", username);
            } else {
                log.warn("STOMP headers中未提供用户名");
            }
        }
        
        return message;
    }

    /**
     * 自定义用户主体类
     */
    private static class WebSocketUserPrincipal implements Principal {
        private final String name;

        public WebSocketUserPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "WebSocketUserPrincipal{name='" + name + "'}";
        }
    }
}