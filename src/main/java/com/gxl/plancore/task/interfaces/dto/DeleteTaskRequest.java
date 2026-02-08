package com.gxl.plancore.task.interfaces.dto;

/**
 * 删除任务请求
 */
public class DeleteTaskRequest {

    private Boolean deleteAll;

    /**
     * 是否删除模板及所有实例
     * 默认 false，仅删除当天
     */
    public Boolean getDeleteAll() {
        return deleteAll;
    }

    /**
     * 设置是否删除模板及所有实例
     */
    public void setDeleteAll(Boolean deleteAll) {
        this.deleteAll = deleteAll;
    }
}
