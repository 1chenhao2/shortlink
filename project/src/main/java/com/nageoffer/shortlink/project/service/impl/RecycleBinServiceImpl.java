package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import com.nageoffer.shortlink.project.service.RecycleBinService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站服务实现类
 */
@Service
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {
    private final StringRedisTemplate stringRedisTemplate;

    public RecycleBinServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 保存回收站
     * @param reqDTO
     */
    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO reqDTO) {
        LambdaUpdateWrapper<ShortLinkDO> eq = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, reqDTO.getGid())
                .eq(ShortLinkDO::getFullShortUrl, reqDTO.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO build = ShortLinkDO.builder()
                .enableStatus(1)
                .build();
        baseMapper.update(build,eq);
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY,reqDTO.getFullShortUrl()));
    }

    /**
     * 分页查询
     * @param reqDTO
     * @return
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO reqDTO) {
        LambdaQueryWrapper<ShortLinkDO> query = Wrappers.lambdaQuery(ShortLinkDO.class)
                .in(ShortLinkDO::getGid, reqDTO.getGids())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0);
        IPage<ShortLinkDO> shortLinkPageReqDTO= baseMapper.selectPage(reqDTO, query);
        return shortLinkPageReqDTO.convert(each-> {
            ShortLinkPageRespDTO result= BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("Http://"+result.getDomain());
            return result;
        });
    }

    /**
     * 恢复回收站
     * @param recycleBinRecoverReqDTO
     */
    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDTO recycleBinRecoverReqDTO) {
        LambdaUpdateWrapper<ShortLinkDO> eq = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, recycleBinRecoverReqDTO.getGid())
                .eq(ShortLinkDO::getFullShortUrl, recycleBinRecoverReqDTO.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO build = ShortLinkDO.builder()
                .enableStatus(0)
                .build();
        baseMapper.update(build,eq);
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY,recycleBinRecoverReqDTO.getFullShortUrl()));
    }

    @Override
    public void removeRecycleBin(RecycleBinRemoveReqDTO recycleBinRemoveReqDTO) {
        LambdaUpdateWrapper<ShortLinkDO> eq = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, recycleBinRemoveReqDTO.getGid())
                .eq(ShortLinkDO::getFullShortUrl, recycleBinRemoveReqDTO.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0);
        baseMapper.delete(eq);
    }
}
