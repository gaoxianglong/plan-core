package com.gxl.plancore.task.interfaces.dto;

/**
 * 更新任务响应
 */
public class UpdateTaskResponse {

    private String id;
    private String title;
    private String priority;
    private String date;
    private String repeatType;
    private Object repeatConfig;
    private String updatedAt;

    /**
     * 构造更新任务响应
     */
    public UpdateTaskResponse(String id, String title, String priority, String date,
                              String repeatType, Object repeatConfig, String updatedAt) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.date = date;
        this.repeatType = repeatType;
        this.repeatConfig = repeatConfig;
        this.updatedAt = updatedAt;
    }

    /**
     * 获取任务ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置任务ID
     */
    public void setId(String id) {
        this.id = id;
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
    public String getDate() {
        return date;
    }

    /**
     * 设置任务日期
     */
    public void setDate(String date) {
        this.date = date;
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
    public Object getRepeatConfig() {
        return repeatConfig;
    }

    /**
     * 设置重复配置
     */
    public void setRepeatConfig(Object repeatConfig) {
        this.repeatConfig = repeatConfig;
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
