package com.nageoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.project.common.convention.exception.ClientException;
import com.nageoffer.shortlink.project.common.enums.VailDateTypeEnum;
import com.nageoffer.shortlink.project.dao.entity.*;
import com.nageoffer.shortlink.project.dao.mapper.*;
import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.*;
import com.nageoffer.shortlink.project.mq.producer.ShortLinkStatsSaveProducer;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import com.nageoffer.shortlink.project.service.UrlTitleService;
import com.nageoffer.shortlink.project.toolkit.HashUtil;
import com.nageoffer.shortlink.project.toolkit.LinkUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.nageoffer.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.nageoffer.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    @Resource(name = "shortUriCreateCachePenetrationBloomFilter")
    private RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;
    private final UrlTitleService urlTitleService;



    @Value("${short-link.stats.locale.amap-key}")
    private String amapKey;
    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    /**
     * 创建短链接
     * @param reqDTO
     * @return
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqDTO) {
        if (StrUtil.isBlank(reqDTO.getDescribe())) {
            reqDTO.setDescribe(urlTitleService.getTitleByUrl(reqDTO.getOriginUrl()));
        }
        String shortLinkSuffix = generateSuffix(reqDTO);// 生成短链接后缀
        String fullShortUrl = new StringBuilder(createShortLinkDefaultDomain).append("/").append(shortLinkSuffix).toString();
        ShortLinkDO shortLinkDO = BeanUtil.toBean(reqDTO, ShortLinkDO.class);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setTotalPv(0);
        shortLinkDO.setTotalUv(0);
        shortLinkDO.setTotalUip(0);
        shortLinkDO.setFullShortUrl(fullShortUrl);
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(shortLinkSuffix)
                .gid(reqDTO.getGid())
                .build();
        try{
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        }catch (DuplicateKeyException ex){
            LambdaQueryWrapper<ShortLinkDO> query = Wrappers.lambdaQuery(ShortLinkDO.class).eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(query);
            if(hasShortLinkDO != null){
                log.warn("短链接{} 重复入库", fullShortUrl);
                throw new RuntimeException("短链接生成重复");
            }
        }
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY,fullShortUrl),
                reqDTO.getOriginUrl(),
                LinkUtil.getLinkCacheValidDate(reqDTO.getValidDate()), TimeUnit.MILLISECONDS);
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(reqDTO.getOriginUrl())
                .gid(reqDTO.getGid())
                .build();
    }

    /**
     * 分页查询
     * @param reqDTO
     * @return
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO reqDTO) {
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(reqDTO);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    /**
     * 查询短链接数量
     * @param gids
     * @return
     */
    @Override
    public List<ShortLinkCountQueryRespDTO> queryShortLinkCount(List<String> gids) {
        var query = Wrappers.query(ShortLinkDO.class)
                .select("gid, COUNT(*) AS shortLinkCount")
                .in("gid", gids)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .groupBy("gid");
        List<Map<String, Object>> maps = baseMapper.selectMaps(query);
        List<ShortLinkCountQueryRespDTO> result = new java.util.ArrayList<>();
        for (Map<String, Object> map : maps) {
            ShortLinkCountQueryRespDTO dto = new ShortLinkCountQueryRespDTO();
            dto.setGid((String) map.get("gid"));
            dto.setShortLinkCount(((Number) map.get("shortLinkCount")).intValue());
            result.add(dto);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(hasShortLinkDO.getDomain())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .favicon(hasShortLinkDO.getFavicon())
                    .createdType(hasShortLinkDO.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            // 为什么监控表要加上Gid？不加的话是否就不存在读写锁？详情查看：https://nageoffer.com/shortlink/question
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            rLock.lock();
            try {
                LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                        .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                        .eq(ShortLinkDO::getDelFlag, 0)
                        .eq(ShortLinkDO::getDelTime, 0L)
                        .eq(ShortLinkDO::getEnableStatus, 0);
                ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                delShortLinkDO.setDelFlag(1);
                baseMapper.update(delShortLinkDO, linkUpdateWrapper);
                ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                        .domain(createShortLinkDefaultDomain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(hasShortLinkDO.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(hasShortLinkDO.getShortUri())
                        .enableStatus(hasShortLinkDO.getEnableStatus())
                        .totalPv(hasShortLinkDO.getTotalPv())
                        .totalUv(hasShortLinkDO.getTotalUv())
                        .totalUip(hasShortLinkDO.getTotalUip())
                        .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                        .favicon(hasShortLinkDO.getFavicon())
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);
                LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDO::getGid, hasShortLinkDO.getGid());
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.delete(linkGotoQueryWrapper);
                shortLinkGotoDO.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDO);
            } finally {
                rLock.unlock();
            }
        }
        // 短链接如何保障缓存和数据库一致性？详情查看：https://nageoffer.com/shortlink/question
        if (!Objects.equals(hasShortLinkDO.getValidDateType(), requestParam.getValidDateType())
                || !Objects.equals(hasShortLinkDO.getValidDate(), requestParam.getValidDate())
                || !Objects.equals(hasShortLinkDO.getOriginUrl(), requestParam.getOriginUrl())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
            Date currentDate = new Date();
            if (hasShortLinkDO.getValidDate() != null && hasShortLinkDO.getValidDate().before(currentDate)) {
                if (Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()) || requestParam.getValidDate().after(currentDate)) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
                }
            }
        }
    }

    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) throws IOException {
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String s = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(s)) {
            shortLinkStats(fullShortUrl, null,request,response);
            ((HttpServletResponse) response).sendRedirect(s);
            return;
        }
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            return;
        }
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            s = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(s)){
                shortLinkStats(fullShortUrl, null,request,response);
                ((HttpServletResponse) response).sendRedirect(s);
                return;

            }
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQuery = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, shortUri);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQuery);
            if (shortLinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),"-",30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> query = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getShortUri, shortUri)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(query);
            if (shortLinkDO != null) {
                if(shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date())){
                    stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),"-",30, TimeUnit.MINUTES);
                    ((HttpServletResponse) response).sendRedirect("/page/notfound");
                    return;
                }
                stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY,fullShortUrl), shortLinkDO.getOriginUrl());
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_SHORT_LINK_KEY,fullShortUrl),
                        shortLinkDO.getOriginUrl(),
                        LinkUtil.getLinkCacheValidDate(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
                shortLinkStats(fullShortUrl, shortLinkDO.getGid(),request,response);
                ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
            } else {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),"-",30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        List<String> originUrls = requestParam.getOriginUrls();
        List<String> describes = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        for (int i = 0; i < originUrls.size(); i++) {
            ShortLinkCreateReqDTO shortLinkCreateReqDTO = BeanUtil.toBean(requestParam, ShortLinkCreateReqDTO.class);
            shortLinkCreateReqDTO.setOriginUrl(originUrls.get(i));
            shortLinkCreateReqDTO.setDescribe(describes.get(i));
            try {
                ShortLinkCreateRespDTO shortLink = createShortLink(shortLinkCreateReqDTO);
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                result.add(linkBaseInfoRespDTO);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i));
            }
        }
        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }

    @Override
    public void shortLinkStats(ShortLinkStatsRecordDTO statsRecord) {
        Map<String, String> producerMap = new HashMap<>();
        producerMap.put("statsRecord", JSON.toJSONString(statsRecord));
        // 消息队列为什么选用RocketMQ？详情查看：https://nageoffer.com/shortlink/question
        shortLinkStatsSaveProducer.send(producerMap);
    }

    private void shortLinkStats(String fullShortUrl,String gid,ServletRequest request,ServletResponse response){
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();

        try{
            AtomicReference<String> uv = new AtomicReference<>();
            //定义了一个“创建 uv Cookie 并写入 Redis”的小任务，先存起来，后面需要时再运行。
            Runnable addResponseCookieTest = () -> {
                uv.set(UUID.randomUUID().toString());
                Cookie uvCookies =new Cookie("uv", uv.get());
                uvCookies.setMaxAge(60 * 60 * 24*30);
                uvCookies.setPath("/");
                ((HttpServletResponse) response).addCookie(uvCookies);
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add("short-link:stats:uv"+fullShortUrl, uv.get());
            };
            // 如果 cookies 不为空，说明客户端有 Cookie，先判断 Cookie 中是否有 uv
            if (ArrayUtil.isNotEmpty(cookies)){
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each ->{
                            uv.set(each);
                            //Redis Set 会去重：
                            //如果 Redis 里以前没有这个 uv，uvadded 大概率是 1
                            //如果 Redis 里已经有这个 uv，uvadded 大概率是 0
                            Long uvadded = stringRedisTemplate.opsForSet().add(("short-link:stats:uv"+fullShortUrl), each);
                            uvFirstFlag.set(uvadded != null && uvadded > 0L);
                        }, addResponseCookieTest);
            }else {
                // 如果 cookies 为空，说明客户端没有 Cookie，先创建一个 uv Cookie 并写入 Redis
                addResponseCookieTest.run();
            }
            // 获取客户端的 IP 地址，并写入 Redis
            String remoteAddr = LinkUtil.getActualIp((HttpServletRequest) request);
            Long uipadded = stringRedisTemplate.opsForSet().add("short-link:stats:uip" + fullShortUrl, remoteAddr);
            boolean uipFirstFlag = uipadded != null && uipadded > 0L;
            // 判断 IP 是否第一次访问
            if(StrUtil.isBlank(gid)){
                // 如果 gid 为空，说明是短链接第一次被访问，需要查询短链接的 gid
                LambdaQueryWrapper<ShortLinkGotoDO> eq = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl.substring(fullShortUrl.lastIndexOf("/") + 1));

                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(eq);
                if (shortLinkGotoDO != null) {
                    gid = shortLinkGotoDO.getGid();
                }
            }
            // 获取当前访问的时间是凌晨几小时
            int hour = DateUtil.hour(new Date(),true);
            // 获取当前访问是星期几
            Week week = DateUtil.dayOfWeekEnum(new Date());
            // 构建访问统计对象
            int weekValue = week.getValue();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid((gid))
                    .date(new Date())
                    .delFlag(0)
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            Map<String,Object> localeParamMap=new HashMap<>();
            // 添加高德地图接口参数
            localeParamMap.put("key", amapKey); // 添加高德地图接口参数
            localeParamMap.put("ip", remoteAddr); // 添加高德地图接口参数
            String localeResuleStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject localeResultObj= JSON.parseObject(localeResuleStr);
            String infoCode = localeResultObj.getString("infocode");
            LinkLocaleStatsDO linkLocaleStatsDO;
            String actualProvince = localeResultObj.getString("province");
            String actualCity = localeResultObj.getString("city");
            if (StrUtil.isNotBlank(infoCode)&& StrUtil.equals(infoCode,"10000")){
                LinkLocaleStatsDO build = LinkLocaleStatsDO.builder()
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(new Date())
                        .cnt(1)
                        .province(actualProvince)
                        .city(actualCity)
                        .adcode(localeResultObj.getString("adcode"))
                        .country("中国")
                        .delFlag(0)
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(build);

            }
            LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .cnt(1)
                    .os(LinkUtil.getOs((HttpServletRequest) request))
                    .delFlag(0)
                    .build();
            linkOsStatsMapper.shortLinkOsStats(linkOsStatsDO);
            // 构建浏览器访问统计对象
            LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                    .browser(LinkUtil.getBrowser(((HttpServletRequest) request)))
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);

            // 构建访问日志对象
            LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                    .user(uv.get())
                    .fullShortUrl(fullShortUrl)
                    .ip(remoteAddr)
                    .network(LinkUtil.getNetwork((HttpServletRequest) request))
                    .device(LinkUtil.getDevice((HttpServletRequest) request))
                    .locale(StrUtil.join("中国",actualProvince,actualCity))
                    .browser(LinkUtil.getBrowser((HttpServletRequest) request))
                    .os(LinkUtil.getOs((HttpServletRequest) request))
                    .gid(gid)
                    .delFlag(0)
                    .build();
            linkAccessLogsMapper.insert(linkAccessLogsDO);
            LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                    .device(LinkUtil.getDevice(((HttpServletRequest) request)))
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
            LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                    .network(LinkUtil.getNetwork(((HttpServletRequest) request)))
                    .cnt(1)
                    .gid(gid)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
            baseMapper.incrementStats(gid,fullShortUrl,1, uvFirstFlag.get() ? 1:0, uipFirstFlag ? 1:0);
            LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .todayPv(1)
                    .todayUv(uvFirstFlag.get() ? 1:0)
                    .todayUip(uipFirstFlag ? 1:0)
                    .build();
            linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
        }catch (Throwable ex){
            log.error("短链接访问量异常", ex);
        }

    }
    /**
     * 生成短链接后缀
     * @param reqDTO
     * @return
     */
    private String generateSuffix(ShortLinkCreateReqDTO reqDTO) {
        String shortUri= null;
        int customGenerateCount = 0;
        while (true) {
            if (customGenerateCount > 10){
                throw new RuntimeException("");
            }
            String originUrl = reqDTO.getOriginUrl();
            originUrl+=System.currentTimeMillis();
            shortUri=HashUtil.hashToBase62(originUrl);
            if(!shortUriCreateCachePenetrationBloomFilter.contains(createShortLinkDefaultDomain+"/"+shortUri)){
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(cn.hutool.core.lang.UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request));
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));
        Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .currentDate(new Date())
                .build();
    }
}
