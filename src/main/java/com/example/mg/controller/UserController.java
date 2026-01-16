package com.example.mg.controller;

import com.example.mg.common.PageData;
import com.example.mg.common.R;
import com.example.mg.dao.UserDAO;
import com.example.mg.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserDAO userDAO;

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
}

