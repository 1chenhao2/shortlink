package com.nageoffer.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsLocaleCNRespDTO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 地区
     */
    private String locale;

    /**
     * 占比
     */
    private Double ratio;
}
