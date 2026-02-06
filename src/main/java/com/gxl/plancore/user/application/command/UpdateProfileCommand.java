package com.gxl.plancore.user.application.command;

/**
 * 更新用户信息命令
 */
public class UpdateProfileCommand {

    private final String userId;
    private final String nickname;
    private final String avatar;

    public UpdateProfileCommand(String userId, String nickname, String avatar) {
        this.userId = userId;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    /**
     * 是否要修改昵称
     */
    public boolean hasNickname() {
        return nickname != null && !nickname.trim().isEmpty();
    }

    /**
     * 是否要修改头像
     */
    public boolean hasAvatar() {
        return avatar != null && !avatar.trim().isEmpty();
    }
}
