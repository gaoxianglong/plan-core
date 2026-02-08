package com.gxl.plancore.task.application.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 创建任务结果
 */
public class CreateTaskResult {

    private final String taskId;
    private final String title;
    private final String priority;
    private final String status;
    private final String date;
    private final String repeatType;
    private final String repeatConfig;
    private final LocalDate repeatEndDate;
    private final Instant createdAt;

    /**
     * 构造创建任务结果
     */
    public CreateTaskResult(String taskId, String title, String priority, String status, String date,
                            String repeatType, String repeatConfig, LocalDate repeatEndDate,
                            Instant createdAt) {
        this.taskId = taskId;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.date = date;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.repeatEndDate = repeatEndDate;
        this.createdAt = createdAt;
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

    public String getDate() {
        return date;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public String getRepeatConfig() {
        return repeatConfig;
    }

    public LocalDate getRepeatEndDate() {
        return repeatEndDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
