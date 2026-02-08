package com.gxl.plancore.task.interfaces.dto;

/**
 * 创建子任务响应
 */
public class CreateSubTaskResponse {

    private String id;
    private String parentId;
    private String title;
    private String status;
    private String repeatType;
    private String createdAt;

    /**
     * 构造创建子任务响应
     */
    public CreateSubTaskResponse(String id, String parentId, String title,
                                  String status, String repeatType, String createdAt) {
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.status = status;
        this.repeatType = repeatType;
        this.createdAt = createdAt;
    }

    /**
     * 获取子任务ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置子任务ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取父任务ID
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * 设置父任务ID
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
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
     * 获取创建时间
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
