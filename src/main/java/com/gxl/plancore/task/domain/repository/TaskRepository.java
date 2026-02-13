package com.gxl.plancore.task.domain.repository;

import java.util.List;
import java.util.Optional;

import com.gxl.plancore.task.domain.entity.Task;
import com.gxl.plancore.task.domain.valueobject.TaskDateCount;

/**
 * 任务仓储接口
 * 领域层定义，由基础设施层实现
 */
public interface TaskRepository {

    /**
     * 保存任务
     *
     * @param task 任务实体
     */
    void save(Task task);

    /**
     * 根据任务ID查询（不含已删除）
     *
     * @param taskId 任务ID
     * @return 任务实体
     */
    Optional<Task> findByTaskId(String taskId);

    /**
     * 更新任务
     *
     * @param task 任务实体
     */
    void update(Task task);

    /**
     * 逻辑删除任务
     *
     * @param taskId 任务ID
     * @return 受影响的行数
     */
    int softDelete(String taskId);

    /**
     * 统计某用户某天的任务数量（不含已删除）
     *
     * @param userId 用户ID
     * @param date   归属日期
     * @return 任务数量
     */
    int countByUserIdAndDate(String userId, String date);

    /**
     * 查询某用户某天的任务列表（不含已删除，按创建时间升序）
     *
     * @param userId 用户ID
     * @param date   归属日期
     * @return 任务列表
     */
    List<Task> findByUserIdAndDate(String userId, String date);

    /**
     * 查询某用户日期范围内的任务列表（不含已删除，按日期升序、创建时间升序）
     *
     * @param userId    用户ID
     * @param startDate 起始日期（含）
     * @param endDate   结束日期（含）
     * @return 任务列表
     */
    List<Task> findByUserIdAndDateRange(String userId, String startDate, String endDate);

    /**
     * 按日期范围聚合统计任务数量（不含已删除）
     * 按 date + priority + status 分组，返回轻量计数结果
     *
     * @param userId    用户ID
     * @param startDate 起始日期（含）
     * @param endDate   结束日期（含）
     * @return 分组计数列表
     */
    List<TaskDateCount> countGroupedByDateRange(String userId, String startDate, String endDate);
}
