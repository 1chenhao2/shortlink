package com.nageoffer.shortlink.project.common.constant;
/**
 * redis key常量
 *
 * @author zhongjie
 */
public class RedisKeyConstant {
    /**
     * 短链跳转前缀Key
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link_goto_%s";
    /**
     * 短链跳转锁前缀 Key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link_lock_goto_%s";

    /**
     * 短链跳转空值Key
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link_is-nulll_goto_%s";
    /**
     * 短链接分组更新锁前缀 Key
     */
    public static final String LOCK_GID_UPDATE_KEY = "short-link_lock_gid-update_%s";
    /**
     * 短链接延迟队列消费统计 Key
     */
    public static final String DELAY_QUEUE_STATS_KEY = "short-link:delay-queue:stats";

    public static final String SHORT_LINK_STATS_UV_KEY = "short-link:stats:uv";
    public static final String SHORT_LINK_STATS_UIP_KEY = "short-link:stats:uip";

    /**
     * 短链接统计 Stream 消息队列 Topic Key
     */
    public static final String SHORT_LINK_STATS_STREAM_TOPIC_KEY = "short-link:stats:stream";

    /**
     * 短链接统计 Stream 消息队列 Group Key
     */
    public static final String SHORT_LINK_STATS_STREAM_GROUP_KEY = "short-link:stats:stream:group";
}

