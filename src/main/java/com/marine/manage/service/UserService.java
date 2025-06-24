package com.marine.manage.service;

import cn.dev33.satoken.stp.StpUtil;
import com.marine.manage.mapper.UserMapper;
import com.marine.manage.pojo.User;
import com.marine.manage.pojo.dto.LoginRequest;
import com.marine.manage.pojo.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /**
     * 获取所有用户 - 只读事务
     */
    public List<User> getAllUsers() {
        log.info("获取所有用户信息");
        return userMapper.getAllUsers();
    }

    /**
     * 用户登录 - 只读事务
     */
    public Map<String, Object> login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        
        log.info("用户登录: {}", email);
        
        User user = userMapper.getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        
        // sa-token登录
        StpUtil.login(user.getId());
        log.info("用户登录成功: {}", user.getId());

        Map<String, Object> loginRes = new HashMap<>();
        loginRes.put("token", StpUtil.getTokenValue());
        loginRes.put("user", user);
        return loginRes;
    }

    /**
     * 用户注册 - 需要事务管理
     * 演示事务的原子性：如果任何步骤失败，整个注册过程都会回滚
     * 由于注册事务需要修改数据库, 因此不能声明为只读事务
     */
    @Transactional
    public String register(RegisterRequest registerRequest) {
        log.info("开始用户注册流程: {}", registerRequest.getEmail());
        
        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();
        String repassword = registerRequest.getRepassword();

        // 验证用户是否已存在
        if (userMapper.getUserByEmail(email) != null) {
            throw new RuntimeException("用户已存在");
        }

        // 验证密码一致性
        if (!password.equals(repassword)) {
            throw new RuntimeException("两次密码不一致");
        }

        User user;
        if (username == null || username.trim().isEmpty()) {
            // 创建用户并生成默认用户名
            user = new User("", email, password);
            userMapper.insertUser(user);
            
            // 更新用户名 - 如果这里失败，整个事务会回滚
            String defaultUsername = "user_" + user.getId();
            userMapper.updateUsernameById(user.getId(), defaultUsername);
            
            log.info("用户注册成功，生成默认用户名: {}", defaultUsername);
            return "注册成功 " + defaultUsername;
        } else {
            // 直接使用提供的用户名
            user = new User(username, email, password);
            userMapper.insertUser(user);
            
            log.info("用户注册成功: {}", username);
            return "注册成功 " + username;
        }
    }

    /**
     * 批量用户操作 - 演示事务传播
     */
    @Transactional
    public void batchUserOperation() {
        log.info("开始批量用户操作");
        
        // 模拟批量操作
        for (int i = 1; i <= 3; i++) {
            try {
                // 调用另一个事务方法
                createTestUser("test" + i + "@example.com", "password" + i);
            } catch (Exception e) {
                log.error("批量操作中第{}个用户创建失败", i, e);
                // 这里可以选择继续或抛出异常来触发回滚
                throw new RuntimeException("批量操作失败", e);
            }
        }
        
        log.info("批量用户操作完成");
    }

    /**
     * 创建测试用户 - 演示事务传播
     */
    @Transactional
    public void createTestUser(String email, String password) {
        log.info("创建测试用户: {}", email);
        
        User user = new User("test_user", email, password);
        userMapper.insertUser(user);
        
        // 模拟可能的业务逻辑
        if (email.contains("error")) {
            throw new RuntimeException("模拟创建用户失败");
        }
        
        log.info("测试用户创建成功: {}", email);
    }

    /**
     * 用户登出
     */
    public void logout() {
        if (!StpUtil.isLogin()) {
            throw new RuntimeException("未登录");
        }
        StpUtil.logout();
        log.info("用户登出成功");
    }
} 