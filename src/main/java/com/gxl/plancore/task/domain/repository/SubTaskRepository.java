package com.gxl.plancore.task.domain.repository;

import com.gxl.plancore.task.domain.entity.SubTask;

import java.util.List;
import java.util.Optional;

/**
 * 子任务仓储接口
 */
public interface SubTaskRepository {

    /**
     * 根据父任务ID列表查询子任务
     */
    List<SubTask> findByParentTaskIds(List<String> parentTaskIds);

    /**
     * 根据父任务ID查询子任务
     */
    List<SubTask> findByParentTaskId(String parentTaskId);

    /**
     * 统计父任务下未完成的子任务数量
     */
    int countIncompleteByParentTaskId(String parentTaskId);

    /**
     * 统计父任务下所有子任务数量（不含已删除）
     */
    int countByParentTaskId(String parentTaskId);

    /**
     * 保存子任务
     */
    void save(SubTask subTask);

    /**
     * 根据子任务ID查询子任务
     */
    Optional<SubTask> findBySubTaskId(String subTaskId);

    /**
     * 更新子任务
     */
    void updateSubTask(SubTask subTask);

    /**
     * 逻辑删除子任务
     */
    void softDelete(String subTaskId);

    /**
     * 更新子任务状态
     */
    void updateStatus(SubTask subTask);
}
