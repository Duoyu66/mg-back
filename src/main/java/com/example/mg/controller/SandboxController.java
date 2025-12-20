package com.example.mg.controller;

import com.example.mg.common.R;
import com.example.mg.dto.SandboxExecuteRequest;
import com.example.mg.service.SandboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 沙箱代码执行控制器
 */
@Tag(name = "代码沙箱", description = "代码执行沙箱相关接口")
@RestController
@RequestMapping("/sandbox")
@RequiredArgsConstructor
public class SandboxController {
    
    private final SandboxService sandboxService;
    
    @Operation(summary = "执行代码", description = "在沙箱环境中执行代码并返回执行结果")
    @PostMapping("/execute")
    public R<String> executeCode(@RequestBody SandboxExecuteRequest request) {
        // 参数验证
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return R.validateFailed("代码不能为空");
        }
        
        if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
            return R.validateFailed("编程语言不能为空");
        }
        
        // 验证语言类型
        String language = request.getLanguage().toLowerCase();
        if (!isValidLanguage(language)) {
            return R.validateFailed("不支持的语言类型，支持的语言：javascript, java");
        }
        
        try {
            // 执行代码，直接返回执行结果
            String result = sandboxService.executeCode(request);
            return R.success(result);
        } catch (RuntimeException e) {
            // 捕获运行时异常，返回错误信息
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = "代码执行失败";
            }
            return R.failed(errorMessage);
        } catch (Exception e) {
            // 捕获其他异常
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = "代码执行失败: " + e.getClass().getSimpleName();
            }
            return R.failed(errorMessage);
        }
    }
    
    /**
     * 验证语言类型是否有效
     */
    private boolean isValidLanguage(String language) {
        return language.equals("javascript") || language.equals("java");
    }
}
