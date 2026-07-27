package com.nageoffer.shortlink.project.dto.req;

import lombok.Data;
/**
 * 恢复短链接请求参数
 */
@Data
public class RecycleBinSaveReqDTO {
    /**
     * 分组id
     */
    private String gid;
    /**
     * 短链接
     */
    private String fullShortUrl;
}
