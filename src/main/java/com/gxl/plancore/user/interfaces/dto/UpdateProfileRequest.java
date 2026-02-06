package com.gxl.plancore.user.interfaces.dto;

import jakarta.validation.constraints.Size;

/**
 * 更新用户信息请求 DTO
 * nickname 和 avatar 至少传一个
 */
public class UpdateProfileRequest {

    @Size(min = 1, max = 20, message = "昵称长度为1-20个字符")
    private String nickname;

    private String avatar;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String nickname, String avatar) {
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
