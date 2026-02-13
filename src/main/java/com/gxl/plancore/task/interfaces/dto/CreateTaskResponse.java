package com.gxl.plancore.task.interfaces.dto;

/**
 * 创建任务响应 DTO
 */
public class CreateTaskResponse {

    private String id;
    private String title;
    private String priority;
    private String status;
    private String date;
    private String createdAt;

    public CreateTaskResponse() {
    }

    public CreateTaskResponse(String id, String title, String priority,
                              String status, String date, String createdAt) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.date = date;
        this.createdAt = createdAt;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
