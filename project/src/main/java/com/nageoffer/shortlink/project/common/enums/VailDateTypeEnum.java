package com.nageoffer.shortlink.project.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 短链有效期类型枚举
 *
 * @author zhongjie
 */
@RequiredArgsConstructor
public enum VailDateTypeEnum {
    /**
     * 永久有效
     */
    PERMANENT(0),
    /**
     * 自定义
     */
    CUSTOM(1);
    @Getter
    private final Integer type;

}
