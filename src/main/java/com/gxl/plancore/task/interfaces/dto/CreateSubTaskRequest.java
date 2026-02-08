package com.gxl.plancore.task.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * 创建子任务请求
 */
public class CreateSubTaskRequest {

    @NotBlank(message = "子任务标题不能为空")
    @Size(min = 1, max = 50, message = "子任务标题长度为1-50个字符")
    private String title;

    private String repeatType;

    private Map<String, Object> repeatConfig;

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
}
