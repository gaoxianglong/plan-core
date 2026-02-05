package com.gxl.plancore.common.response;

import java.time.Instant;
import java.util.UUID;

/**
 * 统一响应结构
 * 符合 API 契约规范
 */
public class ApiResponse<T> {
    
    private int code;
    private String message;
    private T data;
    private String traceId;
    private long timestamp;
    
    private ApiResponse() {
        this.traceId = UUID.randomUUID().toString();
        this.timestamp = Instant.now().toEpochMilli();
    }
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = 0;
        response.message = "success";
        response.data = data;
        return response;
    }
    
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = code;
        response.message = message;
        response.data = null;
        return response;
    }
    
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }
    
    // Getters
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public T getData() {
        return data;
    }
    
    public String getTraceId() {
        return traceId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}
