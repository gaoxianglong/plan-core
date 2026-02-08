package com.gxl.plancore.task.interfaces.dto;

import java.util.Map;

/**
 * 更新任务请求
 */
public class UpdateTaskRequest {

    private String title;
    private String priority;
    private String date;
    private String repeatType;
    private Map<String, Object> repeatConfig;

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
    public Map<String, Object> getRepeatConfig() {
        return repeatConfig;
    }

    /**
     * 设置重复配置
     */
    public void setRepeatConfig(Map<String, Object> repeatConfig) {
        this.repeatConfig = repeatConfig;
    }
}
