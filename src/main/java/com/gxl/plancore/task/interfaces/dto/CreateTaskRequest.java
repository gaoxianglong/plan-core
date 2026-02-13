package com.gxl.plancore.task.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建任务请求 DTO
 */
public class CreateTaskRequest {

    @NotBlank(message = "任务标题不能为空")
    private String title;

    @NotBlank(message = "优先级不能为空")
    private String priority;

    @NotBlank(message = "归属日期不能为空")
    private String date;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(String title, String priority, String date) {
        this.title = title;
        this.priority = priority;
        this.date = date;
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
}
