package com.example.mg.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据库 user 表实体
 */
@Data
@TableName("user")
public class UserEntity {

    @TableId
    private String id;

    private String username;
    private String nickName;
    private String account;
    private String password;

    private String email;
    private String school;
    private String avatar;
    private String job;
    private Integer gender;
    private String mobile;
    private Integer status;
    private Integer vipType;
    private String signature;
    private LocalDateTime vipStartTime;
    private LocalDateTime vipEndTime;
    private Integer exp;
    private Integer points;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String registerIp;
}
