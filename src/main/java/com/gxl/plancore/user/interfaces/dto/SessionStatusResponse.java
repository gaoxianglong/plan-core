package com.gxl.plancore.user.interfaces.dto;

/**
 * 会话状态响应 DTO
 */
public class SessionStatusResponse {

    private boolean valid;
    private String userId;
    private String deviceId;

    public SessionStatusResponse() {
    }

    public SessionStatusResponse(boolean valid, String userId, String deviceId) {
        this.valid = valid;
        this.userId = userId;
        this.deviceId = deviceId;
    }

    public static SessionStatusResponse valid(String userId, String deviceId) {
        return new SessionStatusResponse(true, userId, deviceId);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
