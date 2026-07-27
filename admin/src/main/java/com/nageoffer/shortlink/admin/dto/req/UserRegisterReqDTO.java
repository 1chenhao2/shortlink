package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户注册参数
 */
@Data
public class UserRegisterReqDTO {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String mail;
}
