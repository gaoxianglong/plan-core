package com.gxl.plancore.task.application.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 更新任务结果 DTO
 * 应用层返回给接口层
 */
public class UpdateTaskResult {

    private final String taskId;
    private final String title;
    private final String priority;
    private final LocalDate date;
    private final Instant updatedAt;

    public UpdateTaskResult(String taskId, String title, String priority,
                            LocalDate date, Instant updatedAt) {
        this.taskId = taskId;
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.updatedAt = updatedAt;
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

    public LocalDate getDate() {
        return date;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
