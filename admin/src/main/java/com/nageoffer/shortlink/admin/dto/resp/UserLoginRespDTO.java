package com.nageoffer.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 用户登录返回参数
 */
@Data
@NoArgsConstructor// 无参构造方法
@AllArgsConstructor// 有参构造方法
public class UserLoginRespDTO {
    private String token;

}
