package com.example.mg.dao;

import com.example.mg.dto.UserDTO;

import java.util.List;

/**
 * 用户数据访问接口
 */
public interface UserDAO {

    /**
     * 查询 user 表全部数据
     */
    List<UserDTO> findAll();
}

