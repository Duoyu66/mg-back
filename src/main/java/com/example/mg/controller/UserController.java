package com.example.mg.controller;

import com.example.mg.common.PageData;
import com.example.mg.common.R;
import com.example.mg.dao.UserDAO;
import com.example.mg.dto.LoginRequest;
import com.example.mg.dto.LoginResponse;
import com.example.mg.dto.RegisterRequest;
import com.example.mg.dto.UserDTO;
import com.example.mg.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class UserController {

    private final UserDAO userDAO;
    private final UserService userService;

    @Operation(summary = "获取全部用户", description = "返回 user 表全部记录")
    @GetMapping("/users")
    public List<UserDTO> findAllUsers() {
        return userDAO.findAll();
    }

    @Operation(summary = "通过用户id获取用户信息", description = "根据用户ID查询用户详细信息")
    @PostMapping("/getUserById")
    public R<UserDTO> getUserById(
            @Parameter(description = "用户ID", required = true)
            @RequestBody String id) {
        return userDAO.findById(id);
    }

    @Operation(summary = "分页获取用户列表", description = "根据页码和每页大小分页查询用户列表")
    @PostMapping("/getUserByPage")
    public R<PageData<UserDTO>> getUserByPage(
            @Parameter(description = "页码，从1开始", required = true, example = "1")
            @RequestParam int page,
            @Parameter(description = "每页大小", required = true, example = "10")
            @RequestParam int pageSize) {
        return userDAO.getUserByPage(page, pageSize);
    }

    @Operation(summary = "用户注册", description = "注册新用户并返回Token")
    @PostMapping("/register")
    public R<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = userService.register(request);
            return R.success("注册成功", response);
        } catch (RuntimeException e) {
            return R.failed(e.getMessage());
        }
    }

    @Operation(summary = "用户登录", description = "用户登录并返回Token")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return R.success("登录成功", response);
        } catch (RuntimeException e) {
            return R.failed(e.getMessage());
        }
    }
}

