package com.gxl.plancore.task.interfaces.dto;

import java.util.List;
import java.util.Map;

/**
 * 任务列表响应 DTO
 * 按优先级分组返回
 */
public class TaskListResponse {

    private String date;
    private Map<String, Boolean> hasUncheckedTasks;
    private Map<String, List<TaskResponse>> tasks;

    public TaskListResponse() {
    }

    public TaskListResponse(String date, Map<String, Boolean> hasUncheckedTasks,
                            Map<String, List<TaskResponse>> tasks) {
        this.date = date;
        this.hasUncheckedTasks = hasUncheckedTasks;
        this.tasks = tasks;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Map<String, Boolean> getHasUncheckedTasks() {
        return hasUncheckedTasks;
    }

    public void setHasUncheckedTasks(Map<String, Boolean> hasUncheckedTasks) {
        this.hasUncheckedTasks = hasUncheckedTasks;
    }

    public Map<String, List<TaskResponse>> getTasks() {
        return tasks;
    }

    public void setTasks(Map<String, List<TaskResponse>> tasks) {
        this.tasks = tasks;
    }
}
