package com.example.mg.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 沙箱代码执行响应DTO
 */
@Schema(description = "沙箱代码执行响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SandboxExecuteResponse {
    
    @Schema(description = "执行是否成功", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean success;
    
    @Schema(description = "执行输出", example = "Hello World")
    private String output;
    
    @Schema(description = "错误信息", example = "SyntaxError: Unexpected token")
    private String error;
    
    @Schema(description = "执行时间（毫秒）", example = "123")
    private Long executionTime;
    
    @Schema(description = "内存使用（KB）", example = "1024")
    private Long memoryUsage;
}

