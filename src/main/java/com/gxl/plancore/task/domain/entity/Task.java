package com.gxl.plancore.task.domain.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.gxl.plancore.task.domain.valueobject.TaskPriority;
import com.gxl.plancore.task.domain.valueobject.TaskStatus;

/**
 * 任务聚合根
 * 管理任务的创建、完成、删除等行为
 */
public class Task {

    // 标识
    private final String taskId;
    private final String userId;

    // 基本信息
    private String title;
    private TaskPriority priority;
    private LocalDate date;
    private TaskStatus status;

    // 完成时间
    private Instant completedAt;

    // 审计
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    /**
     * 创建任务（工厂方法）
     *
     * @param userId   用户ID
     * @param title    任务标题
     * @param priority 优先级
     * @param date     归属日期
     * @return 新任务实体
     */
    public static Task create(String userId, String title, TaskPriority priority, LocalDate date) {
        Instant now = Instant.now();
        return new Task(
                UUID.randomUUID().toString(),
                userId,
                title,
                priority,
                date,
                TaskStatus.INCOMPLETE,
                null,
                now,
                now,
                null
        );
    }

    /**
     * 从持久化恢复
     */
    public static Task reconstitute(
            String taskId, String userId,
            String title, TaskPriority priority, LocalDate date, TaskStatus status,
            Instant completedAt, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new Task(
                taskId, userId, title, priority, date, status,
                completedAt, createdAt, updatedAt, deletedAt
        );
    }

    private Task(String taskId, String userId,
                 String title, TaskPriority priority, LocalDate date, TaskStatus status,
                 Instant completedAt, Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.status = status;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * 更新任务信息（仅更新传入的非空字段）
     *
     * @param newTitle    新标题（null 则不更新）
     * @param newPriority 新优先级（null 则不更新）
     * @param newDate     新日期（null 则不更新）
     */
    public void updateInfo(String newTitle, TaskPriority newPriority, LocalDate newDate) {
        if (newTitle != null) {
            this.title = newTitle;
        }
        if (newPriority != null) {
            this.priority = newPriority;
        }
        if (newDate != null) {
            this.date = newDate;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * 完成任务
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = this.completedAt;
    }

    /**
     * 反完成任务
     */
    public void uncomplete() {
        this.status = TaskStatus.INCOMPLETE;
        this.completedAt = null;
        this.updatedAt = Instant.now();
    }

    /**
     * 逻辑删除
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = this.deletedAt;
    }

    /**
     * 判断是否已删除
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // Getters
    public String getTaskId() {
        return taskId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDate getDate() {
        return date;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
