package com.example.mg.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 数据库 user 表实体
 */
@Data
@TableName("user")
public class UserEntity {

    @TableId
    private String id;

    private String username;

    private String email;
}

