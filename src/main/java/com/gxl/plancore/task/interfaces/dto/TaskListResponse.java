package com.gxl.plancore.task.interfaces.dto;

import java.util.List;
import java.util.Map;

/**
 * 任务列表响应 DTO
 * 单日期：date、hasUncheckedTasks、tasks 有值
 * 多日期：dataByDate 有值
 */
public class TaskListResponse {

    private String date;
    private Map<String, Boolean> hasUncheckedTasks;
    private Map<String, List<TaskResponse>> tasks;
    /** 多日期时，key=日期，value=当日数据 */
    private Map<String, PerDateTaskListResponse> dataByDate;

    public TaskListResponse() {
    }

    public TaskListResponse(String date, Map<String, Boolean> hasUncheckedTasks,
                            Map<String, List<TaskResponse>> tasks) {
        this.date = date;
        this.hasUncheckedTasks = hasUncheckedTasks;
        this.tasks = tasks;
        this.dataByDate = null;
    }

    public TaskListResponse(Map<String, PerDateTaskListResponse> dataByDate) {
        this.date = null;
        this.hasUncheckedTasks = null;
        this.tasks = null;
        this.dataByDate = dataByDate;
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

    public Map<String, PerDateTaskListResponse> getDataByDate() {
        return dataByDate;
    }

    public void setDataByDate(Map<String, PerDateTaskListResponse> dataByDate) {
        this.dataByDate = dataByDate;
    }
}
