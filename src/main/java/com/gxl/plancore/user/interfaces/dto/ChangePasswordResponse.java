package com.gxl.plancore.user.interfaces.dto;

/**
 * 修改密码响应 DTO
 */
public class ChangePasswordResponse {

    private String message;

    public ChangePasswordResponse() {
    }

    public ChangePasswordResponse(String message) {
        this.message = message;
    }

    public static ChangePasswordResponse success() {
        return new ChangePasswordResponse("密码修改成功");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
