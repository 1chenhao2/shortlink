package com.nageoffer.shortlink.project.dto.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接分页查询结果
 */
@Data
public class ShortLinkPageRespDTO {

    /**
     * ID
     */
    private Long id;
    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;


    /**
     * 分组标识
     */
    private String gid;

    /**
     * 有效期类型 0：永久有效 1：限时有效
     */
    private Integer validDateType;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validDate;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    /**
     * 描述
     */
    @TableField("`describe`")
    private String describe;
    /**
     * 网站标识
     */
    private String favicon;
    /**
     * 今日PV
     */
    private Integer todayPv;

    /**
     * 今日UV
     */
    private Integer todayUv;

    /**
     * 今日IP数
     */
    private Integer todayUip;

    /**
     * 历史PV
     */
    private Integer totalPv;
    /**
     * 历史UV
     */
    private Integer totalUv;
    /**
     * 历史IP数
     */
    private Integer totalUip;
}
