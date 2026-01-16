package com.example.mg.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("password_history")
public class PasswordHistoryEntity {
    @TableId
    private String id;
    private String userId;
    private String passwordHash;
    private LocalDateTime createdAt;
}
