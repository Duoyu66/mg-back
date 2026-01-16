package com.example.mg.service.impl;

import com.example.mg.dao.UserDAO;
import com.example.mg.dto.LoginRequest;
import com.example.mg.dto.LoginResponse;
import com.example.mg.dto.RegisterRequest;
import com.example.mg.dto.UserDTO;
import com.example.mg.entity.UserEntity;
import com.example.mg.service.UserService;
import com.example.mg.utils.JwtUtil;
import com.example.mg.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        UserDTO existingUser = userDAO.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在（如果需要的话，可以添加邮箱唯一性检查）

        // 创建新用户
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(PasswordUtil.encode(request.getPassword())); // 加密密码

        // 保存用户
        userDAO.save(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 构建返回结果
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return LoginResponse.builder()
                .token(token)
                .user(userDTO)
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据用户名查找用户
        UserDTO userDTO = userDAO.findByUsername(request.getUsername());
        if (userDTO == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 查询用户实体以获取密码
        UserEntity user = getUserEntityById(userDTO.id());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .user(userDTO)
                .build();
    }

    @Override
    public UserDTO findByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    /**
     * 根据ID获取用户实体（内部方法）
     */
    private UserEntity getUserEntityById(String id) {
        return userDAO.getUserEntityById(id);
    }
}

