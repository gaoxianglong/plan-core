package com.gxl.plancore.task.interfaces.dto;

/**
 * 实例化虚拟任务响应
 */
public class MaterializeResponse {

    private String id;
    private String title;
    private String priority;
    private String status;
    private String date;
    private String repeatType;
    private Object repeatConfig;
    private boolean repeatInstance;
    private String repeatParentId;
    private String createdAt;

    public MaterializeResponse(String id, String title, String priority, String status, String date,
                               String repeatType, Object repeatConfig, boolean repeatInstance,
                               String repeatParentId, String createdAt) {
        this.id = id;
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

    public String getId() {
        return id;
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

    public Object getRepeatConfig() {
        return repeatConfig;
    }

    public boolean isRepeatInstance() {
        return repeatInstance;
    }

    public String getRepeatParentId() {
        return repeatParentId;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
