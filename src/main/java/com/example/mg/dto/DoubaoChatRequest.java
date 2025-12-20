package com.example.mg.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 豆包Chat API调用请求DTO
 */
@Schema(description = "豆包Chat API调用请求")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoubaoChatRequest {
    
    @Schema(description = "输入提示词", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，请介绍一下你自己")
    private String prompt;
    
    @Schema(description = "是否使用流式响应", example = "false")
    private Boolean isStream = false;
}

