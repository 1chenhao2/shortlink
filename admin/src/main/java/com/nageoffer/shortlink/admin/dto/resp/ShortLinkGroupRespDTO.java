package com.nageoffer.shortlink.admin.dto.resp;

import lombok.Data;
/**
 * 短链接分组信息
 */
@Data
public class ShortLinkGroupRespDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组排序（数字越小越靠前）
     */
    private Integer sortOrder;
    /**
     * 分组下的短链接数量
     */
    private Integer shortLinkCount;
}
