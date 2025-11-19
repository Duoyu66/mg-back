package com.example.mg.dto;

import lombok.Builder;

/**
 * 对外暴露的用户数据结构
 */
@Builder
public record UserDTO(String id, String username, String email) {

}

