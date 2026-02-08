package com.gxl.plancore.task.application.dto;

import java.time.Instant;

/**
 * 实例化虚拟任务结果
 */
public class MaterializeResult {

    private final String taskId;
    private final String title;
    private final String priority;
    private final String status;
    private final String date;
    private final String repeatType;
    private final String repeatConfig;
    private final boolean repeatInstance;
    private final String repeatParentId;
    private final Instant createdAt;

    public MaterializeResult(String taskId, String title, String priority, String status, String date,
                             String repeatType, String repeatConfig, boolean repeatInstance,
                             String repeatParentId, Instant createdAt) {
        this.taskId = taskId;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.date = date;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.repeatInstance = repeatInstance;
        this.repeatParentId = repeatParentId;
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

    public boolean isRepeatInstance() {
        return repeatInstance;
    }

    public String getRepeatParentId() {
        return repeatParentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
