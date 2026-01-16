package com.example.mg.service;

import com.example.mg.dto.LoginRequest;
import com.example.mg.dto.LoginResponse;
import com.example.mg.dto.RegisterRequest;
import com.example.mg.dto.UserDTO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 根据用户名查找用户
     */
    UserDTO findByUsername(String username);
}

