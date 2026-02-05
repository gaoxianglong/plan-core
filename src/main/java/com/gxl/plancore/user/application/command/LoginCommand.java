package com.gxl.plancore.user.application.command;

/**
 * 登录命令
 */
public class LoginCommand {
    
    private final String email;
    private final String password;
    private final String deviceId;
    private final String deviceName;
    private final String platform;
    private final String osVersion;
    private final String appVersion;
    private final String ipAddress;
    
    public LoginCommand(String email, String password, String deviceId, String deviceName,
                        String platform, String osVersion, String appVersion, String ipAddress) {
        this.email = email;
        this.password = password;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.platform = platform;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.ipAddress = ipAddress;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public String getPlatform() {
        return platform;
    }
    
    public String getOsVersion() {
        return osVersion;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
}
