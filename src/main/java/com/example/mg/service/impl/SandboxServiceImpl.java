package com.example.mg.service.impl;

import com.example.mg.dto.SandboxExecuteRequest;
import com.example.mg.service.SandboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 沙箱代码执行服务实现
 */
@Slf4j
@Service
public class SandboxServiceImpl implements SandboxService {
    
    private static final int DEFAULT_TIMEOUT = 5000; // 默认超时时间5秒
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    
    @Override
    public String executeCode(SandboxExecuteRequest request) {
        int timeout = request.getTimeout() != null ? request.getTimeout() : DEFAULT_TIMEOUT;
        
        try {
            // 根据语言类型执行代码，直接返回执行结果
            return executeByLanguage(request.getCode(), request.getLanguage(), 
                    request.getInput(), timeout);
                    
        } catch (TimeoutException e) {
            throw new RuntimeException("代码执行超时，请检查是否有死循环或无限递归");
            
        } catch (RuntimeException e) {
            // 直接抛出RuntimeException，保留原始错误信息
            throw e;
            
        } catch (Exception e) {
            log.error("代码执行失败", e);
            // 如果有错误消息，直接使用；否则使用默认消息
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = "代码执行失败: " + e.getClass().getSimpleName();
            }
            throw new RuntimeException(errorMessage);
        }
    }
    
    /**
     * 根据语言类型执行代码
     */
    private String executeByLanguage(String code, String language, String input, int timeout) 
            throws Exception {
        return switch (language.toLowerCase()) {
            case "javascript" -> executeJavaScript(code, input, timeout);
            case "java" -> executeJava(code, input, timeout);
            default -> throw new IllegalArgumentException("不支持的语言类型: " + language);
        };
    }
    
    /**
     * 执行JavaScript代码（使用Node.js）
     */
    private String executeJavaScript(String code, String input, int timeout) throws Exception {
        // 检查是否安装了Node.js
        if (!isCommandAvailable("node")) {
            throw new RuntimeException("未安装Node.js，无法执行JavaScript代码");
        }
     
        Path scriptFile = createTempFile("js", code);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("node", scriptFile.toString());
            // 设置环境变量，确保使用UTF-8编码
            processBuilder.environment().put("NODE_ENV", "production");
            processBuilder.environment().put("LANG", "zh_CN.UTF-8");
            processBuilder.environment().put("LC_ALL", "zh_CN.UTF-8");
            
            if (input != null && !input.isEmpty()) {
                processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
            }
            
            Process process = processBuilder.start();
            
            // 如果有输入，写入输入流
            if (input != null && !input.isEmpty()) {
                try (OutputStreamWriter writer = new OutputStreamWriter(
                        process.getOutputStream(), StandardCharsets.UTF_8)) {
                    writer.write(input);
                    writer.flush();
                }
            }
            
            return executeProcess(process, timeout);
        } finally {
            deleteFile(scriptFile);
        }
    }
    
    /**
     * 执行Java代码
     */
    private String executeJava(String code, String input, int timeout) throws Exception {
        // 检查是否安装了JDK
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            throw new RuntimeException("未找到Java运行环境");
        }
        
        // 包装代码为完整的Java类（如果用户只提供了main方法或代码片段）
        String fullJavaCode = wrapJavaCode(code);
        
        // 提取类名
        String className = extractClassName(fullJavaCode);
        if (className == null) {
            throw new RuntimeException("无法从代码中提取类名，请确保代码包含public class定义");
        }
        
        // 创建临时目录用于编译
        Path tempDir = Files.createTempDirectory(Paths.get(TEMP_DIR), "java_sandbox_");
        Path javaFile = tempDir.resolve(className + ".java");
        
        try {
            // 写入Java源文件
            Files.writeString(javaFile, fullJavaCode, StandardCharsets.UTF_8);
            
            // 编译Java文件
            Path javacPath = Paths.get(javaHome, "bin", "javac");
            Process compileProcess = new ProcessBuilder(
                    javacPath.toString(),
                    "-encoding", "UTF-8",
                    javaFile.toString()
            )
            .directory(tempDir.toFile())
            .start();
            
            String compileError = readProcessError(compileProcess);
            if (compileProcess.waitFor() != 0) {
                throw new RuntimeException("编译失败: " + compileError);
            }
            
            // 执行编译后的类
            Path javaPath = Paths.get(javaHome, "bin", "java");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    javaPath.toString(),
                    "-Dfile.encoding=UTF-8",
                    "-Dconsole.encoding=UTF-8",
                    className
            )
            .directory(tempDir.toFile());
            
            // 设置环境变量，确保使用UTF-8编码（不设置JAVA_TOOL_OPTIONS避免提示信息）
            if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.environment().put("LANG", "zh_CN.UTF-8");
                processBuilder.environment().put("LC_ALL", "zh_CN.UTF-8");
            }
            
            if (input != null && !input.isEmpty()) {
                processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
            }
            
            Process process = processBuilder.start();
            
            // 写入输入
            if (input != null && !input.isEmpty()) {
                try (OutputStreamWriter writer = new OutputStreamWriter(
                        process.getOutputStream(), StandardCharsets.UTF_8)) {
                    writer.write(input);
                    writer.flush();
                }
            }
            
            return executeProcess(process, timeout);
        } finally {
            // 清理临时文件
            deleteDirectory(tempDir);
        }
    }
    
    /**
     * 包装Java代码为完整的类
     */
    private String wrapJavaCode(String code) {
        String trimmedCode = code.trim();
        
        // 如果代码已经包含public class
        if (trimmedCode.contains("public class")) {
            // 检查是否已经有main方法
            if (!trimmedCode.contains("public static void main")) {
                // 没有main方法，需要添加一个
                return addMainMethod(trimmedCode);
            }
            // 已经有main方法，直接返回
            return trimmedCode;
        }
        
        // 如果代码包含class但不包含public，添加public
        if (trimmedCode.contains("class ") && !trimmedCode.contains("public class")) {
            String withPublic = trimmedCode.replaceFirst("class ", "public class ");
            // 检查是否有main方法
            if (!withPublic.contains("public static void main")) {
                return addMainMethod(withPublic);
            }
            return withPublic;
        }
        
        // 如果代码只包含main方法，包装成类
        if (trimmedCode.contains("public static void main")) {
            return "public class Main {\n" + trimmedCode + "\n}";
        }
        
        // 默认包装成Main类
        return "public class Main {\n    public static void main(String[] args) {\n" 
                + trimmedCode + "\n    }\n}";
    }
    
    /**
     * 为Java类添加main方法，并调用类中的方法
     */
    private String addMainMethod(String code) {
        // 查找类中的第一个public方法（排除main方法和构造函数）
        // 匹配模式：public [static] [返回类型] 方法名(参数) {
        Pattern methodPattern = Pattern.compile(
            "public\\s+(static\\s+)?[\\w\\[\\]]+\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{",
            Pattern.MULTILINE
        );
        Matcher matcher = methodPattern.matcher(code);
        
        String methodName = null;
        String params = "";
        boolean isStatic = false;
        int methodStart = -1;
        int methodEnd = -1;
        
        // 查找第一个非构造函数、非main的public方法
        while (matcher.find()) {
            String foundMethodName = matcher.group(2);
            // 跳过main方法和构造函数（构造函数的名称首字母大写且与类名相同）
            if (!"main".equals(foundMethodName) && !foundMethodName.matches("^[A-Z]\\w*$")) {
                methodName = foundMethodName;
                params = matcher.group(3);
                isStatic = matcher.group(1) != null && matcher.group(1).trim().equals("static");
                methodStart = matcher.start();
                methodEnd = matcher.end();
                break;
            }
        }
        
        if (methodName == null) {
            // 没有找到合适的方法，创建一个简单的main方法
            return insertMainMethod(code, "System.out.println(\"代码执行成功\");");
        }
        
        // 生成方法调用代码
        StringBuilder callCode = new StringBuilder();
        
        // 根据参数类型生成默认参数值
        if (!params.trim().isEmpty()) {
            String[] paramTypes = params.split(",");
            for (int i = 0; i < paramTypes.length; i++) {
                String paramDef = paramTypes[i].trim();
                // 获取类型部分（去除参数名）
                String paramType = paramDef.split("\\s+")[0];
                
                if (i > 0) {
                    callCode.append(", ");
                }
                
                // 根据类型生成默认值
                if (paramType.contains("[]")) {
                    // 数组类型，例如 int[] -> new int[]{}
                    String baseType = paramType.replace("[]", "");
                    callCode.append("new ").append(baseType).append("[]{}");
                } else {
                    switch (paramType) {
                        case "int":
                            callCode.append("0");
                            break;
                        case "long":
                            callCode.append("0L");
                            break;
                        case "double":
                        case "float":
                            callCode.append("0.0");
                            break;
                        case "boolean":
                            callCode.append("false");
                            break;
                        case "String":
                            callCode.append("\"\"");
                            break;
                        default:
                            callCode.append("null");
                    }
                }
            }
        }
        
        // 生成方法调用语句
        String callStatement;
        if (isStatic) {
            // 静态方法直接调用
            String className = extractClassName(code);
            if (className != null) {
                callStatement = className + "." + methodName + "(" + callCode + ");";
            } else {
                callStatement = methodName + "(" + callCode + ");";
            }
        } else {
            // 非静态方法需要创建实例
            String className = extractClassName(code);
            if (className != null) {
                callStatement = className + " solution = new " + className + "();\n        " +
                              "solution." + methodName + "(" + callCode + ");";
            } else {
                callStatement = methodName + "(" + callCode + ");";
            }
        }
        
        return insertMainMethod(code, callStatement);
    }
    
    /**
     * 在类的末尾（最后一个}之前）插入main方法
     */
    private String insertMainMethod(String code, String methodCall) {
        // 包装System.out以确保UTF-8输出
        // 使用OutputStreamWriter包装System.out，强制UTF-8编码
        String wrappedCode = 
            "java.io.PrintStream originalOut = System.out;\n        " +
            "try {\n            " +
            "java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(System.out, java.nio.charset.StandardCharsets.UTF_8);\n            " +
            "java.io.PrintStream utf8Out = new java.io.PrintStream(writer, true);\n            " +
            "System.setOut(utf8Out);\n            " +
            methodCall + "\n        } finally {\n            " +
            "System.setOut(originalOut);\n        }";
        
        // 找到最后一个 }，在它之前插入main方法
        int lastBraceIndex = code.lastIndexOf('}');
        if (lastBraceIndex == -1) {
            // 没有找到，直接在末尾添加
            return code + "\n    public static void main(String[] args) {\n        " + 
                   wrappedCode + "\n    }\n}";
        }
        
        // 在最后一个 } 之前插入main方法
        String beforeLastBrace = code.substring(0, lastBraceIndex);
        String afterLastBrace = code.substring(lastBraceIndex);
        
        return beforeLastBrace + "\n    public static void main(String[] args) {\n        " + 
               wrappedCode + "\n    }" + afterLastBrace;
    }
    
    /**
     * 从Java代码中提取类名
     */
    private String extractClassName(String code) {
        // 查找 public class 后面的类名
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // 查找 class 后面的类名
        pattern = Pattern.compile("class\\s+(\\w+)");
        matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * 删除目录及其所有内容
     */
    private void deleteDirectory(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                        .sorted(Comparator.reverseOrder())
                        .forEach(this::deleteFile);
            }
        } catch (IOException e) {
            log.warn("删除目录失败: {}", directory, e);
        }
    }
    
    /**
     * 执行进程并获取输出
     */
    private String executeProcess(Process process, int timeout) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> outputFuture = executor.submit(() -> readProcessOutput(process));
            Future<String> errorFuture = executor.submit(() -> readProcessError(process));
            
            // 等待进程完成或超时
            boolean finished = process.waitFor(timeout, TimeUnit.MILLISECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                throw new TimeoutException("执行超时");
            }
            
            String errorOutput = errorFuture.get();
            // readProcessError已经过滤掉了提示信息，如果返回空字符串说明没有真正的错误
            if (errorOutput != null && !errorOutput.trim().isEmpty()) {
                // 真正的错误，抛出异常
                throw new RuntimeException(errorOutput);
            }
            
            return outputFuture.get();
        } finally {
            executor.shutdownNow();
        }
    }
    
    /**
     * 读取进程标准输出（使用字节流确保编码正确）
     */
    private String readProcessOutput(Process process) {
        try {
            // 使用字节流读取，然后转换为UTF-8字符串，确保编码正确
            byte[] buffer = new byte[8192];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = process.getInputStream().read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            byte[] bytes = baos.toByteArray();
            if (bytes.length == 0) {
                return "";
            }
            
            // 首先尝试UTF-8解码
            String result = new String(bytes, StandardCharsets.UTF_8);
            
            // 在Windows系统上，如果检测到可能是GBK编码的中文字节，尝试GBK解码
            if (System.getProperty("os.name").toLowerCase().contains("windows") && 
                containsChineseBytes(bytes)) {
                try {
                    String gbkResult = new String(bytes, "GBK");
                    // 检查UTF-8解码结果是否包含乱码（替换字符）
                    char replacementChar = '\uFFFD'; // UTF-8解码失败的替换字符
                    if (result.indexOf(replacementChar) >= 0 || !isValidUTF8Text(result)) {
                        // UTF-8解码可能有问题，使用GBK解码结果
                        result = gbkResult;
                    }
                } catch (Exception e) {
                    // 如果GBK解码失败，使用UTF-8结果
                }
            }
            
            return result.trim();
        } catch (IOException e) {
            log.error("读取进程输出失败", e);
            return "";
        }
    }
    
    /**
     * 检测字节数组是否包含中文字符的字节序列（GBK编码特征）
     */
    private boolean containsChineseBytes(byte[] bytes) {
        // GBK编码中文字符通常以0x81-0xFE开头，第二个字节是0x40-0xFE
        for (int i = 0; i < bytes.length - 1; i++) {
            int first = bytes[i] & 0xFF;
            int second = bytes[i + 1] & 0xFF;
            if (first >= 0x81 && first <= 0xFE && second >= 0x40 && second <= 0xFE) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查文本是否看起来像有效的UTF-8文本（不包含明显的乱码）
     */
    private boolean isValidUTF8Text(String text) {
        // 检查是否包含替换字符（UTF-8解码失败时的标记）
        if (text.indexOf('\uFFFD') >= 0) {
            return false;
        }
        // 简单检查：如果文本看起来都是可打印字符，认为是有效的
        for (char c : text.toCharArray()) {
            // 允许中文字符、英文字符、数字、常见标点
            if (c >= 0x4E00 && c <= 0x9FFF) {
                continue; // 中文
            }
            if (c >= 0x20 && c <= 0x7E) {
                continue; // ASCII可打印字符
            }
            if (c == '\n' || c == '\r' || c == '\t') {
                continue; // 换行符等
            }
            // 其他Unicode字符也允许
        }
        return true;
    }
    
    /**
     * 读取进程错误输出（使用字节流确保编码正确，并过滤掉提示信息）
     */
    private String readProcessError(Process process) {
        try {
            // 使用字节流读取，然后转换为UTF-8字符串
            byte[] buffer = new byte[8192];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = process.getErrorStream().read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            byte[] bytes = baos.toByteArray();
            if (bytes.length == 0) {
                return "";
            }
            
            // 尝试多种编码方式解码错误信息
            String result = new String(bytes, StandardCharsets.UTF_8).trim();
            
            // 在Windows系统上，如果检测到可能是GBK编码的中文字节，尝试GBK解码
            if (System.getProperty("os.name").toLowerCase().contains("windows") && 
                containsChineseBytes(bytes)) {
                try {
                    String gbkResult = new String(bytes, "GBK").trim();
                    // 检查UTF-8解码结果是否包含乱码（替换字符）
                    char replacementChar = '\uFFFD';
                    if (result.indexOf(replacementChar) >= 0 || !isValidUTF8Text(result)) {
                        result = gbkResult;
                    }
                } catch (Exception e) {
                    // 如果GBK解码失败，使用UTF-8结果
                }
            }
            
            // 过滤掉Java启动时的提示信息
            if (result.startsWith("Picked up JAVA_TOOL_OPTIONS") || 
                result.startsWith("Picked up _JAVA_OPTIONS")) {
                return "";
            }
            
            return result;
        } catch (IOException e) {
            log.error("读取进程错误输出失败", e);
            return "";
        }
    }
    
    /**
     * 创建临时文件
     */
    private Path createTempFile(String extension, String content) throws IOException {
        Path tempFile = Files.createTempFile(Paths.get(TEMP_DIR), 
                "sandbox_", "." + extension);
        Files.writeString(tempFile, content, StandardCharsets.UTF_8);
        return tempFile;
    }
    
    /**
     * 删除文件
     */
    private void deleteFile(Path file) {
        try {
            if (Files.exists(file)) {
                Files.delete(file);
            }
        } catch (IOException e) {
            log.warn("删除临时文件失败: {}", file, e);
        }
    }
    
    /**
     * 检查命令是否可用
     */
    private boolean isCommandAvailable(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version").start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
}

