package com.nageoffer.shortlink.admin.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nageoffer.shortlink.admin.common.serialize.PhoneDesensitizationSerializer;
import lombok.Data;

/**
 * 用户返回参数实体
 */
@Data
public class UserRespDTO {
    private Long id;
    private String username;
    private String realName;
    @JsonSerialize(using = PhoneDesensitizationSerializer.class)// 使用自定义的序列化器
    private String phone;
    private String mail;
}
