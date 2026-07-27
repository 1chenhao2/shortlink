package com.nageoffer.shortlink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dto.req.RecycleBinRecoverReqDTO;
import com.nageoffer.shortlink.admin.dto.req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.*;
import com.nageoffer.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 短链中台远程调用
 */
@FeignClient(value = "short-link-project", url = "${aggregation.remote-url:}")
public interface ShortLinkActualRemoteService {

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO reqDTO);

    /**
     * 批量创建短链接
     */
    @PostMapping("/api/short-link/v1/create/batch")
    Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam);

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/v1/update")
    void updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam);

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@RequestParam("gid") String gid,
                                                     @RequestParam("orderTag") String orderTag,
                                                     @RequestParam("current") Long current,
                                                     @RequestParam("size") Long size);

    /**
     * 查询分组短链接总量
     */
    @GetMapping("/api/short-link/v1/count")
    Result<List<ShortLinkCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("gids") List<String> requestParam);

    /**
     * 根据 URL 获取标题
     */
    @GetMapping("/api/short-link/title")
    Result<String> getTitleByUrl(@RequestParam("url") String url);

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    void saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(@RequestParam("gids") List<String> gidList,
                                                               @RequestParam("current") Long current,
                                                               @RequestParam("size") Long size);

    /**
     * 恢复短链接
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    void recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam);

    /**
     * 移除短链接
     */
    @PostMapping("/api/short-link/v1/recycle-bin/remove")
    void removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam);

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats")
    Result<ShortLinkStatsRespDTO> oneShortLinkStats(@RequestParam("fullShortUrl") String fullShortUrl,
                                                    @RequestParam("gid") String gid,
                                                    @RequestParam("startDate") String startDate,
                                                    @RequestParam("endDate") String endDate,
                                                    @RequestParam("enableStatus") Integer enableStatus);

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats/group")
    Result<ShortLinkStatsRespDTO> groupShortLinkStats(@RequestParam("gid") String gid,
                                                      @RequestParam("startDate") String startDate,
                                                      @RequestParam("endDate") String endDate);

    /**
     * 访问单个短链接指定时间内监控访问记录数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@RequestParam("fullShortUrl") String fullShortUrl,
                                                                               @RequestParam("gid") String gid,
                                                                               @RequestParam("startDate") String startDate,
                                                                               @RequestParam("endDate") String endDate,
                                                                               @RequestParam("current") Long current,
                                                                               @RequestParam("size") Long size);

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(@RequestParam("gid") String gid,
                                                                                    @RequestParam("startDate") String startDate,
                                                                                    @RequestParam("endDate") String endDate,
                                                                                    @RequestParam("current") Long current,
                                                                                    @RequestParam("size") Long size);
}
