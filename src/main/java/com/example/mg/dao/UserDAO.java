package com.example.mg.dao;

import com.example.mg.common.PageData;
import com.example.mg.common.R;
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
    /**
    * 通过用户id获取用户信息
     **/
    R<UserDTO> findById(String id);
    /**
     * 通过分页获取用户列表
     **/
    R<PageData<UserDTO>> getUserByPage(int page, int pageSize);
}

