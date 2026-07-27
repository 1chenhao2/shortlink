package com.nageoffer.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.project.dao.entity.LinkAccessLogsDO;
import lombok.Data;
/**
 * 短链统计访问记录请求参数
 **/
@Data
public class ShortLinkStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {
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
}
