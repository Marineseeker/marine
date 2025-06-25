package com.marine.manage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AuthenticationMapper {
    @Select("""
            SELECT p.name AS permission_name
            FROM user_roles ur
            JOIN role_permissions rp ON ur.role_id = rp.role_id
            JOIN permissions p ON rp.permission_id = p.id
            WHERE ur.user_id = 1;""")
    List<String> getPermissionList(int userId);

    @Select("""
            SELECT r.name AS role_name
            FROM user_roles ur
            JOIN roles r ON ur.role_id = r.id
            WHERE ur.user_id = 1;""")
    List<String> getRoleList(int userId);
}
