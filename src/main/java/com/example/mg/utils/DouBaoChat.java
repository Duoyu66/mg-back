package com.example.mg.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * 豆包（Doubao）Chat API 调用工具类
 * 使用 HTTP 客户端直接调用火山引擎 ARK Chat Completions API
 */
@Slf4j
public class DouBaoChat {
    
    private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";
    private static final String DEFAULT_MODEL = "doubao-seed-1-6-flash-250828";
    private static final String DEFAULT_API_KEY = "f92abeca-039d-4f1f-8f15-e347fbefc055";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    /**
     * 调用豆包Chat API（简化版本，只接收prompt）
     * @param prompt 输入提示词
     * @return 模型响应内容（JSON对象或字符串）
     */
    public static Object callChat(String prompt) {
        // 获取 API Key
        String apiKey = "f92abeca-039d-4f1f-8f15-e347fbefc055";
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = DEFAULT_API_KEY;
        }
        
        try {
            // 构建请求体
            ObjectNode request = OBJECT_MAPPER.createObjectNode();
            request.put("model", DEFAULT_MODEL);
            
            // 构建消息数组
            ArrayNode messagesArray = OBJECT_MAPPER.createArrayNode();
            ObjectNode message = OBJECT_MAPPER.createObjectNode();
            message.put("role", "user");
            
            ArrayNode contentArray = OBJECT_MAPPER.createArrayNode();
            ObjectNode textContent = OBJECT_MAPPER.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", prompt);
            contentArray.add(textContent);
            
            message.set("content", contentArray);
            messagesArray.add(message);
            request.set("messages", messagesArray);
            
            // 将请求对象转换为JSON字符串
            String requestBody = OBJECT_MAPPER.writeValueAsString(request);
            
            // 构建 HTTP 请求
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(120))
                    .build();
            
            // 发送请求
            HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            // 检查响应状态
            if (response.statusCode() != 200) {
                throw new RuntimeException("API 调用失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
            }
            
            // 解析响应
            JsonNode jsonNode = OBJECT_MAPPER.readTree(response.body());
            
            // Chat completions API 响应格式: choices[0].message.content
            if (jsonNode.has("choices") && jsonNode.get("choices").isArray()) {
                ArrayNode choicesArray = (ArrayNode) jsonNode.get("choices");
                if (choicesArray.size() > 0) {
                    JsonNode firstChoice = choicesArray.get(0);
                    if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                        String content = firstChoice.get("message").get("content").asText();
                        // 尝试解析为 JSON 对象，如果失败则返回原始字符串
                        return parseJsonOrString(content);
                    }
                }
            }
            
            // 如果没有找到标准格式，返回完整响应
            return OBJECT_MAPPER.convertValue(jsonNode, Object.class);
            
        } catch (Exception e) {
            throw new RuntimeException("调用豆包模型失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 调用豆包Chat API（流式版本）
     * @param prompt 输入提示词
     * @param emitter SSE发射器，用于发送流式数据
     */
    public static void callChatStream(String prompt, SseEmitter emitter) {
        String apiKey = "f92abeca-039d-4f1f-8f15-e347fbefc055";

        CompletableFuture.runAsync(() -> {
            try {
                // 构建请求体
                ObjectNode request = OBJECT_MAPPER.createObjectNode();
                request.put("model", DEFAULT_MODEL);
                request.put("stream", true);
                
                // 构建消息数组
                ArrayNode messagesArray = OBJECT_MAPPER.createArrayNode();
                ObjectNode message = OBJECT_MAPPER.createObjectNode();
                message.put("role", "user");
                
                ArrayNode contentArray = OBJECT_MAPPER.createArrayNode();
                ObjectNode textContent = OBJECT_MAPPER.createObjectNode();
                textContent.put("type", "text");
                textContent.put("text", prompt);
                contentArray.add(textContent);
                
                message.set("content", contentArray);
                messagesArray.add(message);
                request.set("messages", messagesArray);
                
                // 将请求对象转换为JSON字符串
                String requestBody = OBJECT_MAPPER.writeValueAsString(request);
                
                // 使用 HttpURLConnection 处理流式响应
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "text/event-stream");
                connection.setDoOutput(true);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(120000);
                
                // 发送请求体
                connection.getOutputStream().write(requestBody.getBytes("UTF-8"));
                connection.getOutputStream().flush();
                
                // 检查响应状态
                if (connection.getResponseCode() != 200) {
                    InputStream errorStream = connection.getErrorStream();
                    String errorBody = "";
                    if (errorStream != null) {
                        ByteArrayOutputStream result = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = errorStream.read(buffer)) != -1) {
                            result.write(buffer, 0, length);
                        }
                        errorBody = result.toString("UTF-8");
                    }
                    emitter.completeWithError(new RuntimeException("API 调用失败，状态码: " + connection.getResponseCode() + ", 响应: " + errorBody));
                    return;
                }
                
                // 读取流式响应
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                    String line;
                    StringBuilder contentBuffer = new StringBuilder();
                    
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6); // 移除 "data: " 前缀
                            
                            // 处理流结束标记
                            if ("[DONE]".equals(data.trim())) {
                                emitter.complete();
                                return;
                            }
                            
                            // 解析 JSON 数据
                            try {
                                JsonNode jsonNode = OBJECT_MAPPER.readTree(data);
                                
                                // 提取增量内容
                                if (jsonNode.has("choices") && jsonNode.get("choices").isArray()) {
                                    JsonNode choices = jsonNode.get("choices");
                                    if (choices.size() > 0) {
                                        JsonNode firstChoice = choices.get(0);
                                        if (firstChoice.has("delta") && firstChoice.get("delta").has("content")) {
                                            String deltaContent = firstChoice.get("delta").get("content").asText();
                                            if (deltaContent != null && !deltaContent.isEmpty()) {
                                                contentBuffer.append(deltaContent);
                                                // 发送增量内容
                                                emitter.send(SseEmitter.event()
                                                        .data(deltaContent)
                                                        .name("message"));
                                            }
                                        }
                                        // 检查是否完成
                                        if (firstChoice.has("finish_reason") && 
                                            !firstChoice.get("finish_reason").isNull() &&
                                            !"null".equals(firstChoice.get("finish_reason").asText())) {
                                            // 发送完整内容
                                            String fullContent = contentBuffer.toString();
                                            Object finalContent = parseJsonOrString(fullContent);
                                            emitter.send(SseEmitter.event()
                                                    .data(finalContent)
                                                    .name("done"));
                                            emitter.complete();
                                            return;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // 忽略解析错误，继续处理下一行
                                log.warn("解析SSE数据失败: " + data, e);
                            }
                        }
                    }
                    
                    // 如果正常结束，发送完整内容
                    String fullContent = contentBuffer.toString();
                    if (!fullContent.isEmpty()) {
                        Object finalContent = parseJsonOrString(fullContent);
                        emitter.send(SseEmitter.event()
                                .data(finalContent)
                                .name("done"));
                    }
                    emitter.complete();
                    
                } catch (Exception e) {
                    emitter.completeWithError(new RuntimeException("读取流式响应失败: " + e.getMessage(), e));
                }
                
            } catch (Exception e) {
                emitter.completeWithError(new RuntimeException("调用豆包流式模型失败: " + e.getMessage(), e));
            }
        });
    }
    
    /**
     * 尝试将字符串解析为 JSON 对象，如果失败则返回原始字符串
     * @param text 文本内容
     * @return JSON 对象（Map）或原始字符串
     */
    private static Object parseJsonOrString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        try {
            // 尝试解析为 JSON 对象
            JsonNode jsonNode = OBJECT_MAPPER.readTree(text);
            // 转换为 Java 对象（Map 或 List）
            return OBJECT_MAPPER.convertValue(jsonNode, Object.class);
        } catch (Exception e) {
            // 如果不是有效的 JSON，返回原始字符串
            return text;
        }
    }
}

