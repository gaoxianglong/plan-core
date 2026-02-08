package com.gxl.plancore.task.domain.service;

import com.gxl.plancore.task.domain.entity.Task;
import com.gxl.plancore.task.domain.valueobject.TaskStatus;

import java.time.LocalDate;

/**
 * 虚拟任务构建器
 * 
 * 将重复模板任务转换为指定日期的虚拟任务（用于展示，不持久化）
 */
public class VirtualTaskBuilder {

    /**
     * 基于模板任务构建指定日期的虚拟任务
     * 
     * 虚拟任务特点：
     * - taskId 格式：{模板taskId}_{日期} 便于前端识别
     * - 标记为副本（isRepeatInstance = true）
     * - repeatParentId 指向模板任务
     * - 日期为查询日期
     * - 状态为未完成
     * 
     * @param template  重复模板任务
     * @param queryDate 查询日期
     * @return 虚拟任务（不持久化，仅用于展示）
     */
    public static Task buildVirtualTask(Task template, LocalDate queryDate) {
        // 构建虚拟任务ID：模板ID_日期
        String virtualTaskId = template.getTaskId() + "_" + queryDate.toString();

        // 使用 reconstitute 创建虚拟任务对象
        return Task.reconstitute(
                virtualTaskId,                      // taskId
                template.getUserId(),               // userId
                template.getTitle(),                // title
                template.getPriority(),             // priority
                queryDate,                          // date（使用查询日期）
                TaskStatus.INCOMPLETE,              // status（默认未完成）
                null,                               // completedAt
                template.getRepeatType(),           // repeatType
                template.getRepeatConfig(),         // repeatConfig
                null,                               // repeatEndDate（虚拟任务不需要）
                true,                               // isRepeatInstance
                template.getTaskId(),               // repeatParentId
                template.getCreatedAt(),            // createdAt
                template.getUpdatedAt(),            // updatedAt
                null                                // deletedAt（虚拟任务未删除）
        );
    }
}
