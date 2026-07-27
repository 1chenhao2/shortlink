package com.nageoffer.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * 短链统计请求参数
 */
@Data
public class ShortLinkStatsReqDTO {
    /**
     * 完整的短链地址
     */
    private String fullShortUrl;
    /**
     * 分组ID
     */
    private String gid;
    /**
     * 开始日期
     */
    private String startDate;
    /**
     * 结束日期
     */
    private String endDate;
    /**
     * 启用标识 0：启用 1：未启用
     */
    private Integer enableStatus;
}
