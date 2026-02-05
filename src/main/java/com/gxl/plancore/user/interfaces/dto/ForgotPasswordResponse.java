package com.gxl.plancore.user.interfaces.dto;

/**
 * 找回密码响应 DTO
 */
public class ForgotPasswordResponse {
    
    private String message;
    
    public ForgotPasswordResponse() {
    }
    
    public ForgotPasswordResponse(String message) {
        this.message = message;
    }
    
    public static ForgotPasswordResponse success() {
        return new ForgotPasswordResponse("密码重置邮件已发送，请查收");
    }
    
    // Getter and Setter
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
