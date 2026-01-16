package com.example.mg.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class PostEntity {
    @TableId
    private String id;
    private String content;
    private String userId;
    private String ipAddress;
    private int view;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
