package com.example.mg.controller;

import com.example.mg.common.R;
import com.example.mg.dto.DoubaoChatRequest;
import com.example.mg.utils.DouBaoChat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 豆包AI模型调用控制器
 */
@Slf4j
@Tag(name = "豆包AI", description = "豆包AI模型调用相关接口")
@RestController
@RequestMapping("/doubao")
public class DoubaoController {
    
    @Operation(summary = "调用豆包Chat API", description = "发送提示词到豆包AI模型并获取响应，支持流式和普通两种模式")
    @PostMapping("/chat")
    public Object chat(@RequestBody DoubaoChatRequest request) {
        // 参数验证
        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            return R.validateFailed("提示词不能为空");
        }
        
        // 判断是否使用流式响应
        Boolean isStream = request.getIsStream();
        if (isStream != null && isStream) {
            // 流式响应
            SseEmitter emitter = new SseEmitter(120000L); // 120秒超时
            DouBaoChat.callChatStream(request.getPrompt(), emitter);
            return emitter;
        } else {
            // 普通响应
            try {
                Object response = DouBaoChat.callChat(request.getPrompt());
                return R.success(response);
            } catch (Exception e) {
                log.error("调用豆包模型失败", e);
                return R.failed(e.getMessage() != null ? e.getMessage() : "调用失败");
            }
        }
    }
    
    @Operation(summary = "简单测试（文本）", description = "使用GET方式快速测试豆包模型，仅支持文本输入")
    @GetMapping("/test")
    public R<Object> test(
            @Parameter(description = "提示词", example = "你好，请介绍一下你自己")
            @RequestParam(defaultValue = "你好，请介绍一下你自己") String prompt) {
        try {
            // 调用豆包Chat API，直接返回响应数据到data字段
            Object response = DouBaoChat.callChat(prompt);
            return R.success(response);
                    
        } catch (Exception e) {
            log.error("调用豆包模型失败", e);
            return R.failed(e.getMessage() != null ? e.getMessage() : "调用失败");
        }
    }
}

