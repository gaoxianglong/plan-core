package com.gxl.plancore.user.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求 DTO
 */
public class RegisterRequest {
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32位之间")
    private String password;
    
    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 20, message = "昵称长度必须在1-20字符之间")
    private String nickname;
    
    @Valid
    private DeviceInfoRequest deviceInfo;
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public DeviceInfoRequest getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(DeviceInfoRequest deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
