package com.marine.manage.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，定义拦截规则
        registry.addInterceptor(new SaInterceptor(_ -> {
                    SaRouter.match("/**") // 拦截的路径
                            .check(_ -> StpUtil.checkLogin()); // 校验规则：是否登录
                })).addPathPatterns("/**") // 拦截路径
                .excludePathPatterns("/login", "/register"); // 排除路径
    }
}