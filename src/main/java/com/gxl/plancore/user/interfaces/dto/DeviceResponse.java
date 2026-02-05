package com.gxl.plancore.user.interfaces.dto;

/**
 * 设备信息响应 DTO
 */
public class DeviceResponse {

    private String deviceId;
    private String deviceName;
    private String platform;
    private String lastLoginIp;
    private String lastLoginAt;
    private boolean isCurrent;

    public DeviceResponse() {
    }

    public DeviceResponse(String deviceId, String deviceName, String platform,
                          String lastLoginIp, String lastLoginAt, boolean isCurrent) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.platform = platform;
        this.lastLoginIp = lastLoginIp;
        this.lastLoginAt = lastLoginAt;
        this.isCurrent = isCurrent;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }
}
