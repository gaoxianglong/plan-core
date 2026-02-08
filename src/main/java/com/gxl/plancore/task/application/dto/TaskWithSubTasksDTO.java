package com.gxl.plancore.task.application.dto;

import java.time.Instant;
import java.util.List;

/**
 * 包含子任务的任务 DTO
 */
public class TaskWithSubTasksDTO {

    private final String id;
    private final String title;
    private final String priority;
    private final String status;
    private final String date;
    private final Instant createdAt;
    private final Instant completedAt;
    private final String repeatType;
    private final String repeatConfig;
    private final boolean repeatInstance;
    private final String repeatParentId;
    private final List<SubTaskDTO> subTasks;

    /**
     * 构造包含子任务的任务 DTO
     */
    public TaskWithSubTasksDTO(String id, String title, String priority, String status, String date,
                               Instant createdAt, Instant completedAt, String repeatType, String repeatConfig,
                               boolean repeatInstance, String repeatParentId, List<SubTaskDTO> subTasks) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.date = date;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.repeatInstance = repeatInstance;
        this.repeatParentId = repeatParentId;
        this.subTasks = subTasks;
    }

    /**
     * 获取任务ID
     */
    public String getId() {
        return id;
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
     * 获取任务状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取任务日期
     */
    public String getDate() {
        return date;
    }

    /**
     * 获取创建时间
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取完成时间
     */
    public Instant getCompletedAt() {
        return completedAt;
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
     * 判断是否重复副本
     */
    public boolean isRepeatInstance() {
        return repeatInstance;
    }

    /**
     * 获取重复源任务ID
     */
    public String getRepeatParentId() {
        return repeatParentId;
    }

    /**
     * 获取子任务列表
     */
    public List<SubTaskDTO> getSubTasks() {
        return subTasks;
    }
}
