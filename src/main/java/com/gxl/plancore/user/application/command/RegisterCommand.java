package com.gxl.plancore.user.application.command;

/**
 * 注册命令,作用类似于DTO,用于接收前端请求参数
 */
public class RegisterCommand {
    
    private final String email;
    private final String password;
    private final String nickname;
    private final String ipAddress;
    
    public RegisterCommand(String email, String password, String nickname, String ipAddress) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.ipAddress = ipAddress;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
}
