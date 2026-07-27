package com.nageoffer.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * 短链接查询数量响应DTO
 */
@Data
public class ShortLinkCountQueryRespDTO {
    /**
     * 分组标识
     */
    private String gid;
    /**
     * 短链接数量
     */
    private Integer shortLinkCount;
}
