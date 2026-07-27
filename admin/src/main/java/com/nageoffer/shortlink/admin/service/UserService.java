package com.nageoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<UserDO>{
    /**
     * 获取用户信息
     * @param username
     * @return
     */
    UserRespDTO getUserByUsername(String username);
    /**
     * 判断用户名是否存在
     */
    Boolean hasUsername(String username);
    /**
     * 注册用户
     */
    void register(UserRegisterReqDTO reqDTO);

    /**
     * 修改用户信息
     */
    void update(UserUpdateReqDTO updatereqDTO);

    /**
     * 登录
     */
    UserLoginRespDTO login(UserLoginReqDTO reqDTO);

    /**
     * 验证登录
     */
    Boolean checkLogin(String username, String token);

    /**
     * 登出
     */
    void logout(String username, String token);
}
