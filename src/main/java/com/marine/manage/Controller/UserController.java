package com.marine.manage.Controller;

import com.marine.manage.annotaion.TrackTime;
import com.marine.manage.pojo.Result;
import com.marine.manage.pojo.User;
import com.marine.manage.pojo.dto.LoginRequest;
import com.marine.manage.pojo.dto.RegisterRequest;
import com.marine.manage.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/userlist")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @TrackTime
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest loginRequest) {
        try {
            Map<String, Object> loginRes = userService.login(loginRequest);
            return Result.success(loginRes);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        try {
            userService.logout();
            return Result.success("登出成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("register")
    public Result<String> register(@RequestBody @Valid RegisterRequest registerRequest) {
        try {
            logger.info("Register request: {}", registerRequest);
            String result = userService.register(registerRequest);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 演示批量用户操作的事务
     */
    @PostMapping("/batch-operation")
    public Result<String> batchUserOperation() {
        try {
            userService.batchUserOperation();
            return Result.success("批量操作成功");
        } catch (RuntimeException e) {
            return Result.error("批量操作失败: " + e.getMessage());
        }
    }
}
