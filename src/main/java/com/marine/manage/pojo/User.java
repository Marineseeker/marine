package com.marine.manage.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class User {

    @JsonIgnore
    private int id;

    private String username;

    private String email;

    @JsonIgnore
    // 序列化为json后返回给前端
    // @JsonIgnore标注的属性不会被序列化并返回
    private String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
