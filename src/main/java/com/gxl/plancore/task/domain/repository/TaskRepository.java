package com.gxl.plancore.task.domain.repository;

import com.gxl.plancore.task.domain.entity.Task;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 任务仓储接口
 */
public interface TaskRepository {

    /**
     * 保存任务
     */
    void save(Task task);

    /**
     * 根据任务ID查询任务
     */
    Optional<Task> findByTaskId(String taskId);

    /**
     * 统计用户在指定日期的任务数量
     */
    int countByUserIdAndDate(String userId, LocalDate date);

    /**
     * 查询用户指定日期的所有任务
     */
    java.util.List<Task> findByUserIdAndDate(String userId, LocalDate date);

    /**
     * 查询用户指定日期的未完成任务
     */
    java.util.List<Task> findIncompleteByUserIdAndDate(String userId, LocalDate date);

    /**
     * 查询用户的所有重复模板任务（起始日期 <= 查询日期）
     */
    java.util.List<Task> findRepeatTemplates(String userId, LocalDate queryDate);

    /**
     * 查询指定日期已存在的重复实例
     */
    java.util.List<Task> findRepeatInstancesByDate(String userId, LocalDate date, java.util.List<String> parentIds);

    /**
     * 更新任务
     */
    void update(Task task);

    /**
     * 查询指定日期所有重复实例的父任务ID（用于去重，不受完成状态影响）
     */
    java.util.List<String> findExistingInstanceParentIds(String userId, LocalDate date);

    /**
     * 更新任务信息
     */
    void updateTask(Task task);

    /**
     * 逻辑删除任务
     */
    void softDelete(Task task);

    /**
     * 逻辑删除重复模板的所有关联实例
     * 
     * @return 删除的实例数量
     */
    int softDeleteByParentId(String templateId);

    /**
     * 保存已删除的任务实例（用于虚拟任务删除标记）
     */
    void saveDeletedInstance(Task task);

    /**
     * 更新模板的重复结束日期
     */
    void updateRepeatEndDate(Task task);

    /**
     * 逻辑删除指定模板在某日期及之后的所有实例
     * 
     * @return 删除的实例数量
     */
    int softDeleteInstancesFromDate(String templateId, java.time.LocalDate fromDate);
}
