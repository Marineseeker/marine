package com.marine.manage.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 * 用于在WebSocket连接建立时将HTTP请求参数中的用户名存储到WebSocket会话属性中
 * 弃用：改为通过STOMP headers传递用户名
 */
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

  @Override
  public boolean beforeHandshake(
          @NonNull ServerHttpRequest request,
          @NonNull ServerHttpResponse response,
          @NonNull WebSocketHandler wsHandler,
          @NonNull Map<String, Object> attributes) throws Exception {

    // 从请求参数中获取用户名
    String query = request.getURI().getQuery();
    if (query != null && query.contains("username=")) {
      String username = extractUsernameFromQuery(query);
      if (username != null && !username.isEmpty()) {
        // 将用户名存储到WebSocket会话属性中
        attributes.put("username", username);
        log.info("WebSocket握手 - 用户名: {}", username);
        return true;
      }
    }

    log.warn("WebSocket握手失败 - 未提供有效用户名");
    return false; // 拒绝连接
  }

  @Override
  public void afterHandshake(
          @NonNull ServerHttpRequest request,
          @NonNull ServerHttpResponse response,
          @NonNull WebSocketHandler wsHandler,
          @Nullable Exception exception) {

    if (exception != null) {
      log.error("WebSocket握手异常", exception);
    } else {
      log.info("WebSocket握手成功完成");
    }
  }

  /**
   * 从查询字符串中提取用户名
   */
  private String extractUsernameFromQuery(String query) {
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      if (keyValue.length == 2 && "username".equals(keyValue[0])) {
        try {
          // URL解码
          return java.net.URLDecoder.decode(keyValue[1], "UTF-8");
        } catch (Exception e) {
          log.error("用户名解码失败", e);
          return null;
        }
      }
    }
    return null;
  }
}