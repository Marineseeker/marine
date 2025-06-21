package com.marine.manage.Controller;

import cn.dev33.satoken.stp.StpUtil;
import com.marine.manage.mapper.UserMapper;
import com.marine.manage.pojo.Result;
import com.marine.manage.pojo.User;
import com.marine.manage.pojo.dto.LoginRequest;
import com.marine.manage.pojo.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/userlist")
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        User user = userMapper.getUserByEmail(email);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            return Result.error("密码错误");
        }
        // sa-token登录, 传入email
        StpUtil.login(user.getId());
        logger.info(StpUtil.getLoginIdAsString());

        Map<String, Object> loginRes = new HashMap<>();
        loginRes.put("token", StpUtil.getTokenValue());
        loginRes.put("user", user);
        return Result.success(loginRes);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        if (!StpUtil.isLogin()) {
            return Result.error("未登录");
        }
        StpUtil.logout();
        return Result.success("登出成功");
    }

    @PostMapping("register")
    public Result<String> register(@RequestBody @Valid RegisterRequest registerRequest) {

        logger.info("Register request: {}", registerRequest);

        String username = registerRequest.getUsername();
        String email = registerRequest.getEmail();
        String password = registerRequest.getPassword();
        String repassword = registerRequest.getRepassword();

        if(userMapper.getUserByEmail(email) != null) {
            return Result.error("用户已存在");
        }

        if(!password.equals(repassword)) {
            return Result.error("两次密码不一致");
        }

        if (username == null || username.trim().isEmpty()) {
            User user = new User("", email, password);
            // 在调用 insertUser 时, 回调了 id
            userMapper.insertUser(user);
            userMapper.updateUsernameById(user.getId(), "user_" + user.getId());
            return Result.success("注册成功 " + "user_" + user.getId());
        }else{
            User user = new User(username, email, password);
            userMapper.insertUser(user);
            return Result.success("注册成功 " + username);
        }
    }
}
