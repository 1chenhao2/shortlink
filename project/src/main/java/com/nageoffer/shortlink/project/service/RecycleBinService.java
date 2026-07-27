package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;

/**
 * 回收站服务
 */
public interface RecycleBinService extends IService<ShortLinkDO> {
    /**
     * 保存回收站
     */
    void saveRecycleBin(RecycleBinSaveReqDTO reqDTO);
    /**
     * 分页查询短链接
     *
     * @param reqDTO 分页查询短链接请求参数
     * @return
     */
    IPage<ShortLinkPageRespDTO> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO reqDTO);

    void recoverRecycleBin(RecycleBinRecoverReqDTO recycleBinRecoverReqDTO);

    void removeRecycleBin(RecycleBinRemoveReqDTO recycleBinRemoveReqDTO);
}
