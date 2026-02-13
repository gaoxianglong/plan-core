package com.gxl.plancore.task.application.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 任务 DTO
 * 应用层返回给接口层的单个任务数据
 */
public class TaskDTO {

    private final String taskId;
    private final String title;
    private final String priority;
    private final String status;
    private final LocalDate date;
    private final Instant createdAt;
    private final Instant completedAt;

    public TaskDTO(String taskId, String title, String priority, String status,
                   LocalDate date, Instant createdAt, Instant completedAt) {
        this.taskId = taskId;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.date = date;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
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

    public String getStatus() {
        return status;
    }

    public LocalDate getDate() {
        return date;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
