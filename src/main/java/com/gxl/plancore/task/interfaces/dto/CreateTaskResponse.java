package com.gxl.plancore.task.interfaces.dto;

/**
 * 创建任务响应
 */
public class CreateTaskResponse {

    private String id;
    private String title;
    private String priority;
    private String status;
    private String date;
    private String repeatType;
    private Object repeatConfig;
    private String repeatEndDate;
    private String createdAt;

    /**
     * 构造创建任务响应
     */
    public CreateTaskResponse(String id, String title, String priority, String status, String date,
                              String repeatType, Object repeatConfig, String repeatEndDate,
                              String createdAt) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.date = date;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.repeatEndDate = repeatEndDate;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public Object getRepeatConfig() {
        return repeatConfig;
    }

    public String getRepeatEndDate() {
        return repeatEndDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
