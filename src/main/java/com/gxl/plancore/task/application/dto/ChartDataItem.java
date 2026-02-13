package com.gxl.plancore.task.application.dto;

/**
 * 图表数据项 DTO
 * 统计视图中的单个时间段统计数据
 */
public class ChartDataItem {

    /** 时间标签（周维度为日期，月维度为"第N周"） */
    private final String label;
    /** 已完成任务数 */
    private final int completed;
    /** 未完成任务数 */
    private final int incomplete;
    /** 任务总数 */
    private final int total;
    /** 完成率（百分比，保留2位小数） */
    private final double completionRate;

    public ChartDataItem(String label, int completed, int incomplete, int total, double completionRate) {
        this.label = label;
        this.completed = completed;
        this.incomplete = incomplete;
        this.total = total;
        this.completionRate = completionRate;
    }

    public String getLabel() {
        return label;
    }

    public int getCompleted() {
        return completed;
    }

    public int getIncomplete() {
        return incomplete;
    }

    public int getTotal() {
        return total;
    }

    public double getCompletionRate() {
        return completionRate;
    }
}
