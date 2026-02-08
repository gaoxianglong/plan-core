package com.gxl.plancore.task.application.dto;

import java.time.Instant;

/**
 * 更新任务结果
 */
public class UpdateTaskResult {

    private final String taskId;
    private final String title;
    private final String priority;
    private final String date;
    private final String repeatType;
    private final String repeatConfig;
    private final Instant updatedAt;

    /**
     * 构造更新任务结果
     */
    public UpdateTaskResult(String taskId, String title, String priority, String date,
                            String repeatType, String repeatConfig, Instant updatedAt) {
        this.taskId = taskId;
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.updatedAt = updatedAt;
    }

    /**
     * 获取任务ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 获取任务标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取任务优先级
     */
    public String getPriority() {
        return priority;
    }

    /**
     * 获取任务日期
     */
    public String getDate() {
        return date;
    }

    /**
     * 获取重复类型
     */
    public String getRepeatType() {
        return repeatType;
    }

    /**
     * 获取重复配置
     */
    public String getRepeatConfig() {
        return repeatConfig;
    }

    /**
     * 获取更新时间
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
