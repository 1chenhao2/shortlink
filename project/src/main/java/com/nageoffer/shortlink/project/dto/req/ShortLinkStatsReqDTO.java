package com.nageoffer.shortlink.project.dto.req;

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
     * Enable status: 0 enabled, 1 disabled.
     */
    private Integer enableStatus = 0;
}
