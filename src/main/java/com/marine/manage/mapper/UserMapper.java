package com.marine.manage.mapper;

import com.marine.manage.pojo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * from users")
    List<User> getAllUsers();

    @Select("SELECT * from users where email = #{email}")
    User getUserByEmail(String email);

    @Insert("INSERT INTO users (username, email, password) VALUES (#{username}, #{email}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(User user);

    @Update("UPDATE users SET username = #{randomUsername} WHERE id = #{id}")
    void updateUsernameById(int id, String randomUsername);
}