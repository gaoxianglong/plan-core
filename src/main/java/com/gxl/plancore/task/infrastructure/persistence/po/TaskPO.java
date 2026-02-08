package com.gxl.plancore.task.infrastructure.persistence.po;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 任务持久化对象
 */
public class TaskPO {

    private Long id;
    private String taskId;
    private String userId;
    private String title;
    private String priority;
    private LocalDate date;
    private String status;
    private Instant completedAt;
    private String repeatType;
    private String repeatConfig;
    private LocalDate repeatEndDate;
    private boolean repeatInstance;
    private String repeatParentId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    /**
     * 获取主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取任务ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 设置任务ID
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * 获取用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取任务标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置任务标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取任务优先级
     */
    public String getPriority() {
        return priority;
    }

    /**
     * 设置任务优先级
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * 获取任务日期
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * 设置任务日期
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * 获取任务状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置任务状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取完成时间
     */
    public Instant getCompletedAt() {
        return completedAt;
    }

    /**
     * 设置完成时间
     */
    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * 获取重复类型
     */
    public String getRepeatType() {
        return repeatType;
    }

    /**
     * 设置重复类型
     */
    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    /**
     * 获取重复配置
     */
    public String getRepeatConfig() {
        return repeatConfig;
    }

    /**
     * 设置重复配置
     */
    public void setRepeatConfig(String repeatConfig) {
        this.repeatConfig = repeatConfig;
    }

    /**
     * 获取重复结束日期
     */
    public LocalDate getRepeatEndDate() {
        return repeatEndDate;
    }

    /**
     * 设置重复结束日期
     */
    public void setRepeatEndDate(LocalDate repeatEndDate) {
        this.repeatEndDate = repeatEndDate;
    }

    /**
     * 判断是否重复副本
     */
    public boolean isRepeatInstance() {
        return repeatInstance;
    }

    /**
     * 设置是否重复副本
     */
    public void setRepeatInstance(boolean repeatInstance) {
        this.repeatInstance = repeatInstance;
    }

    /**
     * 获取重复源任务ID
     */
    public String getRepeatParentId() {
        return repeatParentId;
    }

    /**
     * 设置重复源任务ID
     */
    public void setRepeatParentId(String repeatParentId) {
        this.repeatParentId = repeatParentId;
    }

    /**
     * 获取创建时间
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取更新时间
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 获取删除时间
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * 设置删除时间
     */
    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
