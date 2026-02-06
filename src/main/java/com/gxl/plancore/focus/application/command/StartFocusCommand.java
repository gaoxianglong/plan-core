package com.gxl.plancore.focus.application.command;

/**
 * 开始专注命令
 */
public class StartFocusCommand {

    private final String userId;
    private final int durationSeconds;
    private final String type;

    public StartFocusCommand(String userId, int durationSeconds, String type) {
        this.userId = userId;
        this.durationSeconds = durationSeconds;
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getType() {
        return type;
    }
}
