package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;
/**
 * 用户登录参数
 */
@Data
public class UserLoginReqDTO {
    private String username;
    private String password;
}
