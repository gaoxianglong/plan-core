package com.gxl.plancore.task.application.command;

/**
 * 更新任务命令
 */
public class UpdateTaskCommand {

    private final String userId;
    private final String taskId;
    private final String title;
    private final String priority;
    private final String date;

    public UpdateTaskCommand(String userId, String taskId, String title,
                             String priority, String date) {
        this.userId = userId;
        this.taskId = taskId;
        this.title = title;
        this.priority = priority;
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public String getTaskId() {
        return taskId;
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
