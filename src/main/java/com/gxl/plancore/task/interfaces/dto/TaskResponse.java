package com.gxl.plancore.task.interfaces.dto;

import java.util.List;

/**
 * 任务响应（包含子任务）
 */
public class TaskResponse {

    private String id;
    private String title;
    private String priority;
    private String status;
    private String date;
    private String createdAt;
    private String completedAt;
    private String repeatType;
    private Object repeatConfig;
    private boolean isRepeatInstance;
    private String repeatParentId;
    private List<SubTaskResponse> subTasks;

    /**
     * 构造任务响应
     */
    public TaskResponse(String id, String title, String priority, String status, String date,
                        String createdAt, String completedAt, String repeatType, Object repeatConfig,
                        boolean isRepeatInstance, String repeatParentId, List<SubTaskResponse> subTasks) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.status = status;
        this.date = date;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.isRepeatInstance = isRepeatInstance;
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
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取完成时间
     */
    public String getCompletedAt() {
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
    public Object getRepeatConfig() {
        return repeatConfig;
    }

    /**
     * 判断是否重复副本
     */
    public boolean isRepeatInstance() {
        return isRepeatInstance;
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
    public List<SubTaskResponse> getSubTasks() {
        return subTasks;
    }
}
