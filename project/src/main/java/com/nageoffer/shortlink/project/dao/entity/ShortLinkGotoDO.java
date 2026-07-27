package com.nageoffer.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接跳转表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_goto")
public class ShortLinkGotoDO {
    private Long id;
    @TableField("full_short_url")
    private String fullShortUrl;
    private String gid;
}
