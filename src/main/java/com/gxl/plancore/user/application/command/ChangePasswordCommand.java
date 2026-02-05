package com.gxl.plancore.user.application.command;

/**
 * 修改密码命令
 */
public class ChangePasswordCommand {

    private final String userId;
    private final String currentDeviceId;
    private final String oldPassword;
    private final String newPassword;

    public ChangePasswordCommand(String userId, String currentDeviceId, 
                                  String oldPassword, String newPassword) {
        this.userId = userId;
        this.currentDeviceId = currentDeviceId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public String getUserId() {
        return userId;
    }

    public String getCurrentDeviceId() {
        return currentDeviceId;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
