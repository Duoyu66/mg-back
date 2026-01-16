package com.example.mg.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostVO {
    private String id;
    private String content;
    private String userId;
    private String ipAddress;
    private int view;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private int likeCount;
    private int commentCount;
    private String avatar;
    private String school;
    private String signature;
    private String nickname;
}
