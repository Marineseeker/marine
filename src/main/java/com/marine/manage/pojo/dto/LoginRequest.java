package com.marine.manage.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 16, message = "密码长度为6-16位")
    private String password;
}
