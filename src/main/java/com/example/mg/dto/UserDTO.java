package com.example.mg.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 对外暴露的用户数据结构
 */
@Schema(description = "用户信息DTO")
@Builder
public record UserDTO(
        @Schema(description = "用户ID")
        String id,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "头像URL")
        String avatar
) {
}

