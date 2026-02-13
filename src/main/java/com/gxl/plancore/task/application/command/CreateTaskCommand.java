package com.gxl.plancore.task.application.command;

/**
 * 创建任务命令
 */
public class CreateTaskCommand {

    private final String userId;
    private final String title;
    private final String priority;
    private final String date;

    public CreateTaskCommand(String userId, String title, String priority, String date) {
        this.userId = userId;
        this.title = title;
        this.priority = priority;
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getPriority() {
        return priority;
    }

    public String getDate() {
        return date;
    }
}
