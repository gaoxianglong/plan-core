package com.gxl.plancore.task.interfaces.dto;

/**
 * 更新任务响应 DTO
 */
public class UpdateTaskResponse {

    private String id;
    private String title;
    private String priority;
    private String date;
    private String updatedAt;

    public UpdateTaskResponse() {
    }

    public UpdateTaskResponse(String id, String title, String priority,
                              String date, String updatedAt) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
