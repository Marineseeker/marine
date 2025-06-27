package com.marine.manage.config;

import cn.dev33.satoken.stp.StpInterface;
import com.marine.manage.mapper.AuthenticationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    private final AuthenticationMapper authenticationMapper;
    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        int userId = Integer.parseInt(loginId.toString());
        return authenticationMapper.getPermissionList(userId);
    }
    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        int userId = Integer.parseInt(loginId.toString());
        return authenticationMapper.getRoleList(userId);
    }
}