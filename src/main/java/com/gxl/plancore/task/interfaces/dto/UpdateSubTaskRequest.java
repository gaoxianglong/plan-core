package com.gxl.plancore.task.interfaces.dto;

import java.util.Map;

/**
 * 更新子任务请求
 */
public class UpdateSubTaskRequest {

    private String title;
    private String repeatType;
    private Map<String, Object> repeatConfig;
    /** 操作日期（重复任务按日隔离，格式 YYYY-MM-DD，非重复任务可不传） */
    private String date;

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

    /**
     * 获取操作日期
     */
    public String getDate() {
        return date;
    }

    /**
     * 设置操作日期
     */
    public void setDate(String date) {
        this.date = date;
    }
}
