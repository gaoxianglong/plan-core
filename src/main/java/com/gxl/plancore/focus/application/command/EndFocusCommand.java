package com.gxl.plancore.focus.application.command;

/**
 * 结束专注命令
 */
public class EndFocusCommand {

    private final String userId;
    private final String sessionId;
    private final int elapsedSeconds;
    private final String endType;

    public EndFocusCommand(String userId, String sessionId, int elapsedSeconds, String endType) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.elapsedSeconds = elapsedSeconds;
        this.endType = endType;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public String getEndType() {
        return endType;
    }
}
