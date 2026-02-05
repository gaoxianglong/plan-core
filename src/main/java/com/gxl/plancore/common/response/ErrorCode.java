package com.gxl.plancore.common.response;

/**
 * 错误码枚举
 * 符合 API 契约规范中定义的错误码
 */
public enum ErrorCode {
    
    // 全局错误码
    SUCCESS(0, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    // 认证模块 1001-1099
    AUTH_LOGIN_FAILED(1001, "邮箱或密码错误"),
    AUTH_DEVICE_LIMIT(1003, "设备数量超出上限"),
    AUTH_EMAIL_EXISTS(1004, "邮箱已被注册"),
    AUTH_PASSWORD_INVALID(1005, "密码格式不正确"),
    AUTH_NICKNAME_FORBIDDEN(1006, "昵称包含违规词"),
    AUTH_EMAIL_NOT_FOUND(1007, "邮箱未注册"),
    
    // 设备管理 2001-2099
    DEVICE_CANNOT_LOGOUT_CURRENT(2001, "不能踢出当前设备"),
    DEVICE_NOT_FOUND(2002, "设备不存在"),
    
    // 任务管理 3001-3099
    TASK_TITLE_INVALID(3001, "任务标题为空或超长"),
    TASK_DATE_OUT_OF_RANGE(3002, "日期超出范围"),
    TASK_DAILY_LIMIT(3003, "单日任务数超出上限"),
    TASK_REPEAT_CONFIG_INVALID(3004, "重复配置格式错误"),
    TASK_HAS_INCOMPLETE_SUBTASKS(3005, "父任务存在未完成子任务"),
    TASK_PARENT_NOT_FOUND(3006, "父任务不存在"),
    TASK_SUBTASK_LIMIT(3007, "子任务数量超出上限"),
    
    // 专注模块 4001-4099
    FOCUS_SESSION_EXISTS(4001, "存在进行中的会话"),
    
    // 用户中心 5001-5099
    USER_NICKNAME_FORBIDDEN(5001, "昵称包含违规词"),
    USER_NICKNAME_TOO_FREQUENT(5002, "昵称修改过于频繁");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
