package com.example.mg.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLogEntity {
    @TableId
    private String id;
    private String userId;
    private String action;
    private Integer success;
    private String message;
    private String ip;
    private String ua;
    private LocalDateTime createdAt;
}
