package com.example.mg.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mg.common.PageData;
import com.example.mg.common.R;
import com.example.mg.dao.UserDAO;
import com.example.mg.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @folder 我用来测试的
 */

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class UserController {

    private final UserDAO userDAO;

    /**
     * GET /api/test/users
     * 返回 user 表全部记录
     * @name 获取全部用户
     */
    @GetMapping("/users")
    public List<UserDTO> findAllUsers() {
        return userDAO.findAll();
    }

    /**
    * @name 通过用户id获取用户信息
     * */
    @PostMapping("/getUserById")
    public R<UserDTO> getUserById(@RequestParam String id) {
        return userDAO.findById(id);
    }
    /**
     * @name 分页获取用户列表
     * **/
    @PostMapping("/getUserByPage")
    public  R<PageData<UserDTO>>  getUserByPage(@RequestParam int page, @RequestParam int pageSize) {
        return userDAO.getUserByPage(page,pageSize);

    }
}

