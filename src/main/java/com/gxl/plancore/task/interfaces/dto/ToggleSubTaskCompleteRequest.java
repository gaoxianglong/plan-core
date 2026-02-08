package com.gxl.plancore.task.interfaces.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 完成/反完成子任务请求
 */
public class ToggleSubTaskCompleteRequest {

    @NotNull(message = "completed 不能为空")
    private Boolean completed;

    /** 操作日期（重复任务按日隔离，格式 YYYY-MM-DD，非重复任务可不传） */
    private String date;

    /**
     * 获取完成状态
     */
    public Boolean getCompleted() {
        return completed;
    }

    /**
     * 设置完成状态
     */
    public void setCompleted(Boolean completed) {
        this.completed = completed;
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
