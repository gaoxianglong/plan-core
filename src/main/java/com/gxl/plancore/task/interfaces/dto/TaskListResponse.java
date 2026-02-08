package com.gxl.plancore.task.interfaces.dto;

import java.util.List;
import java.util.Map;

/**
 * 任务列表响应
 */
public class TaskListResponse {

    private String date;
    private Map<String, Boolean> hasUncheckedTasks;
    private Map<String, List<TaskResponse>> tasks;

    /**
     * 构造任务列表响应
     */
    public TaskListResponse(String date, Map<String, Boolean> hasUncheckedTasks,
                            Map<String, List<TaskResponse>> tasks) {
        this.date = date;
        this.hasUncheckedTasks = hasUncheckedTasks;
        this.tasks = tasks;
    }

    /**
     * 获取查询日期
     */
    public String getDate() {
        return date;
    }

    /**
     * 获取各象限是否有未完成任务
     */
    public Map<String, Boolean> getHasUncheckedTasks() {
        return hasUncheckedTasks;
    }

    /**
     * 获取按象限分组的任务列表
     */
    public Map<String, List<TaskResponse>> getTasks() {
        return tasks;
    }
}
