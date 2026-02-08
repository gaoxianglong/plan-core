package com.gxl.plancore.task.interfaces.dto;

/**
 * 完成/反完成子任务响应
 */
public class ToggleSubTaskCompleteResponse {

    private String id;
    private String status;
    private String completedAt;
    private ParentTaskInfo parentTask;

    /**
     * 构造完成/反完成子任务响应
     */
    public ToggleSubTaskCompleteResponse(String id, String status, String completedAt,
                                          ParentTaskInfo parentTask) {
        this.id = id;
        this.status = status;
        this.completedAt = completedAt;
        this.parentTask = parentTask;
    }

    /**
     * 获取子任务ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置子任务ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取子任务状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置子任务状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取完成时间
     */
    public String getCompletedAt() {
        return completedAt;
    }

    /**
     * 设置完成时间
     */
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * 获取父任务信息
     */
    public ParentTaskInfo getParentTask() {
        return parentTask;
    }

    /**
     * 设置父任务信息
     */
    public void setParentTask(ParentTaskInfo parentTask) {
        this.parentTask = parentTask;
    }

    /**
     * 父任务信息
     */
    public static class ParentTaskInfo {

        private String id;
        private String status;

        /**
         * 构造父任务信息
         */
        public ParentTaskInfo(String id, String status) {
            this.id = id;
            this.status = status;
        }

        /**
         * 获取父任务ID
         */
        public String getId() {
            return id;
        }

        /**
         * 设置父任务ID
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * 获取父任务状态
         */
        public String getStatus() {
            return status;
        }

        /**
         * 设置父任务状态
         */
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
