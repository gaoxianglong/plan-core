package com.gxl.plancore.common.exception;

import com.gxl.plancore.common.response.ErrorCode;

/**
 * 业务异常
 * 用于可预期的业务规则校验失败
 */
public class BusinessException extends RuntimeException {
    
    private final int code;
    private final String message;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.message = customMessage;
    }
    
    public int getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
