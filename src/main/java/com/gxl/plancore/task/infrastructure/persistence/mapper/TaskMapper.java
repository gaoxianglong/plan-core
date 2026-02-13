package com.gxl.plancore.task.infrastructure.persistence.mapper;

import java.util.List;

import com.gxl.plancore.task.infrastructure.persistence.po.TaskCountPO;
import com.gxl.plancore.task.infrastructure.persistence.po.TaskPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 任务 MyBatis Mapper
 */
@Mapper
public interface TaskMapper {

    String COLUMNS = "task_id, user_id, title, priority, date, status, " +
            "completed_at, created_at, updated_at, deleted_at";

    /**
     * 插入任务
     */
    @Insert("INSERT INTO task (task_id, user_id, title, priority, date, status, " +
            "completed_at, created_at, updated_at, deleted_at) " +
            "VALUES (#{taskId}, #{userId}, #{title}, #{priority}, #{date}, #{status}, " +
            "#{completedAt}, #{createdAt}, #{updatedAt}, #{deletedAt})")
    int insert(TaskPO po);

    /**
     * 根据任务ID查询（不含已删除）
     */
    @Select("SELECT " + COLUMNS + " FROM task WHERE task_id = #{taskId} AND deleted_at IS NULL")
    TaskPO findByTaskId(@Param("taskId") String taskId);

    /**
     * 更新任务（标题、优先级、日期、状态、完成时间、更新时间）
     */
    @Update("UPDATE task SET title = #{title}, priority = #{priority}, date = #{date}, " +
            "status = #{status}, completed_at = #{completedAt}, updated_at = #{updatedAt} " +
            "WHERE task_id = #{taskId} AND deleted_at IS NULL")
    int update(TaskPO po);

    /**
     * 逻辑删除任务
     */
    @Update("UPDATE task SET deleted_at = NOW(3), updated_at = NOW(3) " +
            "WHERE task_id = #{taskId} AND deleted_at IS NULL")
    int softDelete(@Param("taskId") String taskId);

    /**
     * 统计某用户某天的任务数量（不含已删除）
     */
    @Select("SELECT COUNT(*) FROM task WHERE user_id = #{userId} AND date = #{date} AND deleted_at IS NULL")
    int countByUserIdAndDate(@Param("userId") String userId, @Param("date") String date);

    /**
     * 查询某用户某天的任务列表（不含已删除，按创建时间升序）
     */
    @Select("SELECT " + COLUMNS + " FROM task " +
            "WHERE user_id = #{userId} AND date = #{date} AND deleted_at IS NULL " +
            "ORDER BY created_at ASC")
    List<TaskPO> findByUserIdAndDate(@Param("userId") String userId, @Param("date") String date);

    /**
     * 查询某用户日期范围内的任务列表（不含已删除，按日期升序、创建时间升序）
     */
    @Select("SELECT " + COLUMNS + " FROM task " +
            "WHERE user_id = #{userId} AND date >= #{startDate} AND date <= #{endDate} " +
            "AND deleted_at IS NULL " +
            "ORDER BY date ASC, created_at ASC")
    List<TaskPO> findByUserIdAndDateRange(@Param("userId") String userId,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate);

    /**
     * 按日期范围聚合统计任务数量（不含已删除，按 date + priority + status 分组）
     * 用于统计视图图表数据，避免加载全量任务对象
     */
    @Select("SELECT date, priority, status, COUNT(*) AS task_count FROM task " +
            "WHERE user_id = #{userId} AND date >= #{startDate} AND date <= #{endDate} " +
            "AND deleted_at IS NULL " +
            "GROUP BY date, priority, status")
    List<TaskCountPO> countGroupedByDateRange(@Param("userId") String userId,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate);
}
