package com.nageoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
//DO:
import java.time.LocalDateTime;
@Data// 自动生成getter和setter方法
@TableName("t_user")// 表名
public class UserDO {
        private Long id;
        private String username;
        private String password;
        private String realName;
        private String phone;
        private String mail;
        @TableField(fill = FieldFill.UPDATE)
        private Long deletionTime;
        @TableField(fill = FieldFill.INSERT)
        private LocalDateTime createTime;
        @TableField(fill = FieldFill.INSERT_UPDATE)
        private LocalDateTime updateTime;
        @TableField(fill = FieldFill.INSERT)
        //删除标识：0-正常，1-删除
        private Integer delFlag;
}
