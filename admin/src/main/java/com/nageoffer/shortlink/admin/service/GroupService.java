package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;


public interface GroupService extends IService<GroupDO> {
    /**
     * 新增分组
     * @param groupName
     */
    void saveGroup(String groupName);

    /**
     * 新增分组
     * @param username
     * @param groupName
     */
    void saveGroup(String username,String groupName);

    /**
     * 查询用户短链接分组集合
     * @return
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改分组
     * @return
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO reqDTO);

    /**
     * 删除分组
     * @param gid
     */
    void deleteGroup(String gid);

    /**
     * 分组排序
     * @param reqDTO
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> reqDTO);
}
