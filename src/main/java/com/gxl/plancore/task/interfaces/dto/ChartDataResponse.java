package com.gxl.plancore.task.interfaces.dto;

/**
 * 图表数据项响应 DTO
 */
public class ChartDataResponse {

    /** 时间标签 */
    private String label;
    /** 已完成任务数 */
    private int completed;
    /** 未完成任务数 */
    private int incomplete;
    /** 任务总数 */
    private int total;
    /** 完成率（百分比） */
    private double completionRate;

    public ChartDataResponse() {
    }

    public ChartDataResponse(String label, int completed, int incomplete, int total, double completionRate) {
        this.label = label;
        this.completed = completed;
        this.incomplete = incomplete;
        this.total = total;
        this.completionRate = completionRate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getIncomplete() {
        return incomplete;
    }

    public void setIncomplete(int incomplete) {
        this.incomplete = incomplete;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }
}
