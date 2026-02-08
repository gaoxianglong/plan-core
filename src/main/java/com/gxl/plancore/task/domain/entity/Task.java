package com.gxl.plancore.task.domain.entity;

import com.gxl.plancore.task.domain.valueobject.RepeatType;
import com.gxl.plancore.task.domain.valueobject.TaskPriority;
import com.gxl.plancore.task.domain.valueobject.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 任务聚合根
 */
public class Task {

    private final String taskId;
    private final String userId;
    private String title;
    private TaskPriority priority;
    private LocalDate date;
    private TaskStatus status;
    private Instant completedAt;
    private RepeatType repeatType;
    private String repeatConfig;
    private LocalDate repeatEndDate;
    private boolean repeatInstance;
    private String repeatParentId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    /**
     * 构造任务聚合根
     */
    private Task(String taskId, String userId, String title, TaskPriority priority, LocalDate date,
                 TaskStatus status, Instant completedAt, RepeatType repeatType, String repeatConfig,
                 LocalDate repeatEndDate, boolean repeatInstance, String repeatParentId,
                 Instant createdAt, Instant updatedAt, Instant deletedAt) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.status = status;
        this.completedAt = completedAt;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.repeatEndDate = repeatEndDate;
        this.repeatInstance = repeatInstance;
        this.repeatParentId = repeatParentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    /**
     * 创建新任务
     */
    public static Task create(String userId, String title, TaskPriority priority, LocalDate date,
                              RepeatType repeatType, String repeatConfig, LocalDate repeatEndDate) {
        Instant now = Instant.now();
        return new Task(
                UUID.randomUUID().toString(),
                userId,
                title,
                priority,
                date,
                TaskStatus.INCOMPLETE,
                null,
                repeatType,
                repeatConfig,
                repeatEndDate,
                false,
                null,
                now,
                now,
                null
        );
    }

    /**
     * 基于模板创建重复实例
     */
    public static Task createInstance(Task template, LocalDate instanceDate) {
        Instant now = Instant.now();
        return new Task(
                UUID.randomUUID().toString(),
                template.getUserId(),
                template.getTitle(),
                template.getPriority(),
                instanceDate,
                TaskStatus.INCOMPLETE,
                null,
                template.getRepeatType(),
                template.getRepeatConfig(),
                null,
                true,
                template.getTaskId(),
                now,
                now,
                null
        );
    }

    /**
     * 基于模板创建已删除的重复实例
     * 用于虚拟任务的删除标记
     */
    public static Task createDeletedInstance(Task template, LocalDate instanceDate) {
        Instant now = Instant.now();
        return new Task(
                UUID.randomUUID().toString(),
                template.getUserId(),
                template.getTitle(),
                template.getPriority(),
                instanceDate,
                TaskStatus.INCOMPLETE,
                null,
                template.getRepeatType(),
                template.getRepeatConfig(),
                null,
                true,
                template.getTaskId(),
                now,
                now,
                now
        );
    }

    /**
     * 通过持久化数据重建任务
     */
    public static Task reconstitute(String taskId, String userId, String title, TaskPriority priority,
                                    LocalDate date, TaskStatus status, Instant completedAt,
                                    RepeatType repeatType, String repeatConfig, LocalDate repeatEndDate,
                                    boolean repeatInstance, String repeatParentId,
                                    Instant createdAt, Instant updatedAt, Instant deletedAt) {
        return new Task(taskId, userId, title, priority, date, status, completedAt, repeatType,
                repeatConfig, repeatEndDate, repeatInstance, repeatParentId, createdAt, updatedAt, deletedAt);
    }

    /**
     * 获取任务ID
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 获取用户ID
     */
    public String getUserId() {
        return userId;
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
    public TaskPriority getPriority() {
        return priority;
    }

    /**
     * 获取任务日期
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * 获取任务状态
     */
    public TaskStatus getStatus() {
        return status;
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
    public RepeatType getRepeatType() {
        return repeatType;
    }

    /**
     * 获取重复配置
     */
    public String getRepeatConfig() {
        return repeatConfig;
    }

    /**
     * 获取重复结束日期
     */
    public LocalDate getRepeatEndDate() {
        return repeatEndDate;
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
     * 获取创建时间
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取更新时间
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 完成任务
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 反完成任务（取消完成状态）
     */
    public void uncomplete() {
        this.status = TaskStatus.INCOMPLETE;
        this.completedAt = null;
        this.updatedAt = Instant.now();
    }

    /**
     * 判断任务是否已完成
     */
    public boolean isCompleted() {
        return this.status == TaskStatus.COMPLETED;
    }

    /**
     * 更新任务标题
     */
    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新任务优先级
     */
    public void updatePriority(TaskPriority priority) {
        this.priority = priority;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新任务日期
     */
    public void updateDate(LocalDate date) {
        this.date = date;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新重复设置
     */
    public void updateRepeat(RepeatType repeatType, String repeatConfig) {
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新重复结束日期
     */
    public void updateRepeatEndDate(LocalDate repeatEndDate) {
        this.repeatEndDate = repeatEndDate;
        this.updatedAt = Instant.now();
    }

    /**
     * 获取删除时间
     */
    public Instant getDeletedAt() {
        return deletedAt;
    }

    /**
     * 逻辑删除任务
     */
    public void delete() {
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 判断任务是否已删除
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * 判断是否为重复模板任务
     */
    public boolean isRepeatTemplate() {
        return this.repeatType != RepeatType.NONE && !this.repeatInstance;
    }
}
