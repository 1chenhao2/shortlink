package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {
    /**
     * 创建短链接
     *
     * @param reqDTO 创建短链接请求参数
     * @return
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqDTO);

    /**
     * 分页查询短链接
     *
     * @param reqDTO 分页查询短链接请求参数
     * @return
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO reqDTO);

    /**
     * 查询短链接数量
     *
     * @param gids 分组标识列表
     * @return
     */
    List<ShortLinkCountQueryRespDTO> queryShortLinkCount(List<String> gids);

    /**
     * 修改短链接
     *
     * @param reqDTO 修改短链接请求参数
     */
    void updateShortLink(ShortLinkUpdateReqDTO reqDTO);

    /**
     * 短链接跳转
     *
     * @param shortUri 短链接
     * @param request  请求
     * @param response 响应
     * @return
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) throws IOException;

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);
    /**
     * 短链接统计（消息队列消费）
     *
     * @param statsRecord 短链接统计实体参数
     */
    void shortLinkStats(ShortLinkStatsRecordDTO statsRecord);
}
