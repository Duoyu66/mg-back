package com.example.mg.service;

import com.example.mg.dto.SandboxExecuteRequest;

/**
 * 沙箱代码执行服务接口
 */
public interface SandboxService {
    
    /**
     * 在沙箱环境中执行代码
     * @param request 执行请求
     * @return 执行结果（代码的输出内容）
     */
    String executeCode(SandboxExecuteRequest request);
}

