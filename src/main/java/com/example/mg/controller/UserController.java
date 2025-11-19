package com.example.mg.controller;

import com.example.mg.dao.UserDAO;
import com.example.mg.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供 MyBatis-Plus 测试接口
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class UserController {

    private final UserDAO userDAO;

    /**
     * GET /api/test/users
     * 返回 user 表全部记录
     */
    @GetMapping("/users")
    public List<UserDTO> findAllUsers() {
        return userDAO.findAll();
    }
}

