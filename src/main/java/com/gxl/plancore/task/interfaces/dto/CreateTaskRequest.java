package com.gxl.plancore.task.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * 创建任务请求
 */
public class CreateTaskRequest {

    @NotBlank(message = "任务标题不能为空")
    @Size(min = 1, max = 100, message = "任务标题长度为1-100个字符")
    private String title;

    @NotBlank(message = "优先级不能为空")
    private String priority;

    @NotBlank(message = "日期不能为空")
    private String date;

    private String repeatType;

    private Map<String, Object> repeatConfig;

    private String repeatEndDate;

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
     * 获取优先级
     */
    public String getPriority() {
        return priority;
    }

    /**
     * 设置优先级
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

    /**
     * 获取重复结束日期
     */
    public String getRepeatEndDate() {
        return repeatEndDate;
    }

    /**
     * 设置重复结束日期
     */
    public void setRepeatEndDate(String repeatEndDate) {
        this.repeatEndDate = repeatEndDate;
    }
}
