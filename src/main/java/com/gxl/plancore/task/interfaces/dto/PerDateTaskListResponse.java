package com.gxl.plancore.task.interfaces.dto;

import java.util.List;
import java.util.Map;

/**
 * 单日任务列表响应 DTO
 * 用于多日期查询时 dataByDate 的 value
 */
public class PerDateTaskListResponse {

    private Map<String, Boolean> hasUncheckedTasks;
    private Map<String, List<TaskResponse>> tasks;

    public PerDateTaskListResponse() {
    }

    public PerDateTaskListResponse(Map<String, Boolean> hasUncheckedTasks,
                                   Map<String, List<TaskResponse>> tasks) {
        this.hasUncheckedTasks = hasUncheckedTasks;
        this.tasks = tasks;
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
