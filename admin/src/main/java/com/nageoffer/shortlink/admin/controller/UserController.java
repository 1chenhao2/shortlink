package com.nageoffer.shortlink.admin.controller;

import com.nageoffer.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息
     * @param username
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/actual/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        UserRespDTO userRespDTO = userService.getUserByUsername(username);
        if (userRespDTO == null){
            throw new ClientException(BaseErrorCode.USER_NOT_FOUND_ERROR);
        }else{
            return Results.success(userRespDTO);
        }
    }
    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userService.hasUsername(username));
    }
    /**
     * 创建用户
     */
    @PostMapping("/api/short-link/admin/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO reqDTO){
        userService.register(reqDTO);
        return Results.success();
    }
    /**
     * 修改用户信息
     */
    @PutMapping("/api/short-link/admin/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO UpdatereqDTO){
        userService.update(UpdatereqDTO);
        return Results.success();
    }
    /**
     * 登录
     */
    @PostMapping("/api/short-link/admin/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO reqDTO){
        UserLoginRespDTO result = userService.login(reqDTO);
        return Results.success(result);
    }
    /**
     * 验证登录
     */

    @GetMapping("/api/short-link/admin/v1/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username,@RequestParam("token") String token){
        return Results.success(userService.checkLogin(username, token));
    }
    /**
     * 登出
     */
    @DeleteMapping("/api/short-link/admin/v1/user/logout")
    public Result<Void> logout(@RequestParam("username") String username,@RequestParam("token") String token){
        userService.logout(username, token);
        return Results.success();
    }
}
