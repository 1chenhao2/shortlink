package com.nageoffer.shortlink.admin.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor// 自动生成getter和setter方法
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    @Resource(name = "userRegisterCachePenetrationBloomFilter")
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;
    /**
     * 根据用户名查询用户信息
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(eq);
        if (userDO == null) {
            return null;
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }
    @Override
    public Boolean hasUsername(String  username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }
    /**
     * 注册
     */
    @Override
    public void register(UserRegisterReqDTO reqDTO) {
        if (hasUsername(reqDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        RLock lock = redissonClient.getLock("userRegisterCachePenetrationLock"+reqDTO.getUsername());
        try {
            if (lock.tryLock()){
                int insert = baseMapper.insert(BeanUtil.toBean(reqDTO, UserDO.class));
                if (insert <= 0) {
                    throw new RuntimeException("注册失败");
                }
                userRegisterCachePenetrationBloomFilter.add(reqDTO.getUsername());
                groupService.saveGroup(reqDTO.getUsername(),"默认分组");
                return;
            }

            throw new RuntimeException("用户名已存在");
        }finally {
            lock.unlock();
        }

    }
    @Override
    public void update(UserUpdateReqDTO updatereqDTO) {
        //TODO 验证当前用户是否为登录用户
        LambdaQueryWrapper<UserDO> eq = Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, updatereqDTO.getUsername());
        //创建一个针对 UserDO 表的条件构造器，里面装了一个条件：username 字段等于 updatereqDTO.getUsername() 的值。
        //LambdaQueryWrapper:MyBatis-Plus 提供的一个条件构造器类,一个专门用来"包装查询条件"的工具
        //.eq:条件构造器的方法，表示"等于","添加一个条件：字段等于某个值"
        baseMapper.update(BeanUtil.toBean(updatereqDTO, UserDO.class), eq);
    }
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO reqDTO) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, reqDTO.getUsername())
                .eq(UserDO::getPassword, reqDTO.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries("short-link:login:" + reqDTO.getUsername());
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            stringRedisTemplate.expire("short-link:login:" + reqDTO.getUsername(), 30L, TimeUnit.MINUTES);
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            return new UserLoginRespDTO(token);
        }
        /**
         * Hash
         * Key:login_用户名
         * Value:
         *   key:token标识
         *   value:JSON字符串（用户信息）
         */
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("short-link:login:"+reqDTO.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire("short-link:login:"+reqDTO.getUsername(), 30, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid);
    }
    /**
     * 验证登录
     */
    @Override
    public Boolean checkLogin(String username, String token){
        return stringRedisTemplate.opsForHash().get("short-link:login:"+username, token)!=null;
    }
    /**
     * 登出
     */
    @Override
    public void logout(String username, String token) {
        if (!checkLogin(username, token)){
            throw new RuntimeException("用户未登录或者用户Token不存在");
        }
        stringRedisTemplate.opsForHash().delete("short-link:login:"+username, token);
    }
}
