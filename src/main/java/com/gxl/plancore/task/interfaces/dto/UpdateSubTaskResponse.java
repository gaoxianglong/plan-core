package com.gxl.plancore.task.interfaces.dto;

/**
 * 更新子任务响应
 */
public class UpdateSubTaskResponse {

    private String id;
    private String title;
    private String updatedAt;

    /**
     * 构造更新子任务响应
     */
    public UpdateSubTaskResponse(String id, String title, String updatedAt) {
        this.id = id;
        this.title = title;
        this.updatedAt = updatedAt;
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
     * 获取更新时间
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
