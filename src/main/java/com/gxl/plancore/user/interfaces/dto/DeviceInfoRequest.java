package com.gxl.plancore.user.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 设备信息请求 DTO
 */
public class DeviceInfoRequest {
    
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;
    
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;
    
    @NotBlank(message = "平台不能为空")
    private String platform;
    
    private String osVersion;
    
    @NotBlank(message = "APP版本号不能为空")
    private String appVersion;
    
    // Getters and Setters
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
    
    public String getOsVersion() {
        return osVersion;
    }
    
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}
