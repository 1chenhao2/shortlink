package com.nageoffer.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nageoffer.shortlink.project.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {
    /**
     * 今日统计自增
     */
    @Insert("INSERT INTO t_link_stats_today (full_short_url, gid, date, today_pv, today_uv, today_uip, create_time, update_time, del_flag) " +
            "VALUES (#{statsToday.fullShortUrl}, #{statsToday.gid}, #{statsToday.date}, #{statsToday.todayPv}, #{statsToday.todayUv}, #{statsToday.todayUip}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE today_pv = today_pv + #{statsToday.todayPv}, today_uv = today_uv + #{statsToday.todayUv}, today_uip = today_uip + #{statsToday.todayUip}")
    void shortLinkTodayState(@Param("statsToday") LinkStatsTodayDO statsTodayDO);
}
