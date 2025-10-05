package com.marine.manage.service.chat;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OnlineUserService {
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();

    public void userOnline(String username, String sessionId) {
        onlineUsers.put(username, sessionId);
    }

    public void userOffline(String username) {
        onlineUsers.remove(username);
    }

    public Map<String, String> getOnlineUsers() {
        return new ConcurrentHashMap<>(onlineUsers);
    }

    public boolean isOnline(String username) {
        return onlineUsers.containsKey(username);
    }

    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
}
