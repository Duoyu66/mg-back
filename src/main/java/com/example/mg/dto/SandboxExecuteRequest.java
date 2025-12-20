package com.example.mg.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 沙箱代码执行请求DTO
 */
@Schema(description = "沙箱代码执行请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxExecuteRequest {
    
    @Schema(description = "要执行的代码", requiredMode = Schema.RequiredMode.REQUIRED, example = "console.log('Hello World');")
    private String code;
    
    @Schema(description = "编程语言", requiredMode = Schema.RequiredMode.REQUIRED, example = "javascript", 
            allowableValues = {"javascript", "java"})
    private String language;
    
    @Schema(description = "输入数据（可选）", example = "test input")
    private String input;
    
    @Schema(description = "超时时间（毫秒），默认5000ms", example = "5000")
    private Integer timeout;
}

