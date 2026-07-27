package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;
/**
 * 用户更新参数
 */
@Data
public class UserUpdateReqDTO {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String mail;
}
