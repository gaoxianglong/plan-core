package com.gxl.plancore.user.application.command;

/**
 * 找回密码命令
 */
public class ForgotPasswordCommand {
    
    private final String email;
    
    public ForgotPasswordCommand(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
}
