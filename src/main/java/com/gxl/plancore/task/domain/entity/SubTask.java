package com.gxl.plancore.task.domain.entity;

import com.gxl.plancore.task.domain.valueobject.RepeatType;
import com.gxl.plancore.task.domain.valueobject.TaskStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * 子任务实体
 */
public class SubTask {

    private final String subTaskId;
    private final String parentTaskId;
    private final String userId;
    private String title;
    private TaskStatus status;
    private Instant completedAt;
    private RepeatType repeatType;
    private String repeatConfig;
    private boolean repeatInstance;
    private String repeatParentId;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 构造子任务实体
     */
    private SubTask(String subTaskId, String parentTaskId, String userId, String title,
                    TaskStatus status, Instant completedAt, RepeatType repeatType, String repeatConfig,
                    boolean repeatInstance, String repeatParentId, Instant createdAt, Instant updatedAt) {
        this.subTaskId = subTaskId;
        this.parentTaskId = parentTaskId;
        this.userId = userId;
        this.title = title;
        this.status = status;
        this.completedAt = completedAt;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.repeatInstance = repeatInstance;
        this.repeatParentId = repeatParentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 创建新子任务（默认不重复）
     */
    public static SubTask create(String parentTaskId, String userId, String title) {
        Instant now = Instant.now();
        return new SubTask(
                UUID.randomUUID().toString(),
                parentTaskId,
                userId,
                title,
                TaskStatus.INCOMPLETE,
                null,
                RepeatType.NONE,
                null,
                false,
                null,
                now,
                now
        );
    }

    /**
     * 创建新子任务（指定重复设置）
     */
    public static SubTask create(String parentTaskId, String userId, String title,
                                  RepeatType repeatType, String repeatConfig) {
        Instant now = Instant.now();
        return new SubTask(
                UUID.randomUUID().toString(),
                parentTaskId,
                userId,
                title,
                TaskStatus.INCOMPLETE,
                null,
                repeatType,
                repeatConfig,
                false,
                null,
                now,
                now
        );
    }

    /**
     * 基于模板子任务创建实例副本（用于重复任务按日隔离）
     * 
     * @param templateSubTask 模板子任务
     * @param instanceParentTaskId 实例父任务ID
     */
    public static SubTask createInstance(SubTask templateSubTask, String instanceParentTaskId) {
        Instant now = Instant.now();
        return new SubTask(
                UUID.randomUUID().toString(),
                instanceParentTaskId,
                templateSubTask.getUserId(),
                templateSubTask.getTitle(),
                templateSubTask.getStatus(),
                templateSubTask.getCompletedAt(),
                templateSubTask.getRepeatType(),
                templateSubTask.getRepeatConfig(),
                true,
                templateSubTask.getSubTaskId(),
                now,
                now
        );
    }

    /**
     * 通过持久化数据重建子任务
     */
    public static SubTask reconstitute(String subTaskId, String parentTaskId, String userId, String title,
                                       TaskStatus status, Instant completedAt, RepeatType repeatType,
                                       String repeatConfig, boolean repeatInstance, String repeatParentId,
                                       Instant createdAt, Instant updatedAt) {
        return new SubTask(subTaskId, parentTaskId, userId, title, status, completedAt,
                repeatType, repeatConfig, repeatInstance, repeatParentId, createdAt, updatedAt);
    }

    /**
     * 获取子任务ID
     */
    public String getSubTaskId() {
        return subTaskId;
    }

    /**
     * 获取父任务ID
     */
    public String getParentTaskId() {
        return parentTaskId;
    }

    /**
     * 获取用户ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 获取子任务标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取子任务状态
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
     * 判断是否重复副本
     */
    public boolean isRepeatInstance() {
        return repeatInstance;
    }

    /**
     * 获取重复源子任务ID
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
     * 完成子任务
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * 反完成子任务（取消完成状态）
     */
    public void uncomplete() {
        this.status = TaskStatus.INCOMPLETE;
        this.completedAt = null;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新子任务标题
     */
    public void updateTitle(String title) {
        this.title = title;
        this.updatedAt = Instant.now();
    }

    /**
     * 更新子任务重复设置
     */
    public void updateRepeat(RepeatType repeatType, String repeatConfig) {
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.updatedAt = Instant.now();
    }
}
