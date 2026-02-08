package com.gxl.plancore.task.infrastructure.persistence.po;

import java.time.Instant;

/**
 * 子任务持久化对象
 */
public class SubTaskPO {

    private Long id;
    private String subTaskId;
    private String parentTaskId;
    private String userId;
    private String title;
    private String status;
    private Instant completedAt;
    private String repeatType;
    private String repeatConfig;
    private boolean repeatInstance;
    private String repeatParentId;
    private Instant createdAt;
    private Instant updatedAt;

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
     * 获取子任务ID
     */
    public String getSubTaskId() {
        return subTaskId;
    }

    /**
     * 设置子任务ID
     */
    public void setSubTaskId(String subTaskId) {
        this.subTaskId = subTaskId;
    }

    /**
     * 获取父任务ID
     */
    public String getParentTaskId() {
        return parentTaskId;
    }

    /**
     * 设置父任务ID
     */
    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
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
     * 获取子任务标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置子任务标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取子任务状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置子任务状态
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
     * 获取重复源子任务ID
     */
    public String getRepeatParentId() {
        return repeatParentId;
    }

    /**
     * 设置重复源子任务ID
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
}
