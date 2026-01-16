package com.example.mg.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("refresh_token")
public class RefreshTokenEntity {
    @TableId
    private String id;
    private String userId;
    private String tokenHash;
    private String deviceFp;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Integer revoked;
}
