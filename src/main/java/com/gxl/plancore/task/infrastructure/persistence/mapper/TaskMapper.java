package com.gxl.plancore.task.infrastructure.persistence.mapper;

import com.gxl.plancore.task.infrastructure.persistence.po.TaskPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

/**
 * 任务 Mapper
 */
@Mapper
public interface TaskMapper {

    /** 所有查询共用的字段列表 */
    String COLUMNS = "id, task_id, user_id, title, priority, date, status, completed_at, " +
            "repeat_type, repeat_config, repeat_end_date, is_repeat_instance, repeat_parent_id, " +
            "created_at, updated_at, deleted_at";

    /**
     * 插入任务记录
     */
    @Insert("INSERT INTO task(task_id, user_id, title, priority, date, status, completed_at, " +
            "repeat_type, repeat_config, repeat_end_date, is_repeat_instance, repeat_parent_id, " +
            "created_at, updated_at) " +
            "VALUES(#{taskId}, #{userId}, #{title}, #{priority}, #{date}, #{status}, #{completedAt}, " +
            "#{repeatType}, #{repeatConfig}, #{repeatEndDate}, #{repeatInstance}, #{repeatParentId}, " +
            "#{createdAt}, #{updatedAt})")
    int insert(TaskPO po);

    /**
     * 根据任务ID查询任务
     */
    @Select("SELECT " + COLUMNS + " FROM task WHERE task_id = #{taskId} AND deleted_at IS NULL")
    TaskPO findByTaskId(@Param("taskId") String taskId);

    /**
     * 统计用户指定日期的任务数量
     */
    @Select("SELECT COUNT(1) FROM task WHERE user_id = #{userId} AND date = #{date} AND deleted_at IS NULL")
    int countByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);

    /**
     * 查询用户指定日期的所有任务（按创建时间升序）
     */
    @Select("SELECT " + COLUMNS + " FROM task WHERE user_id = #{userId} AND date = #{date} " +
            "AND deleted_at IS NULL ORDER BY created_at ASC")
    java.util.List<TaskPO> findByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);

    /**
     * 查询用户指定日期的未完成任务（按创建时间升序）
     */
    @Select("SELECT " + COLUMNS + " FROM task WHERE user_id = #{userId} AND date = #{date} " +
            "AND status = 'INCOMPLETE' AND deleted_at IS NULL ORDER BY created_at ASC")
    java.util.List<TaskPO> findIncompleteByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);

    /**
     * 查询用户的所有重复模板任务（起始日期 <= 查询日期，且未过期）
     */
    @Select("SELECT " + COLUMNS + " FROM task WHERE user_id = #{userId} " +
            "AND repeat_type != 'NONE' AND is_repeat_instance = 0 " +
            "AND date <= #{queryDate} " +
            "AND (repeat_end_date IS NULL OR repeat_end_date >= #{queryDate}) " +
            "AND deleted_at IS NULL ORDER BY created_at ASC")
    java.util.List<TaskPO> findRepeatTemplates(@Param("userId") String userId, @Param("queryDate") LocalDate queryDate);

    /**
     * 查询指定日期已存在的重复实例（根据 repeat_parent_id 列表）
     */
    @Select("<script>" +
            "SELECT " + COLUMNS + " FROM task WHERE user_id = #{userId} AND date = #{date} " +
            "AND is_repeat_instance = 1 AND repeat_parent_id IN " +
            "<foreach collection='parentIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "AND deleted_at IS NULL" +
            "</script>")
    java.util.List<TaskPO> findRepeatInstancesByDate(@Param("userId") String userId,
                                                      @Param("date") LocalDate date,
                                                      @Param("parentIds") java.util.List<String> parentIds);

    /**
     * 更新任务状态和完成时间
     */
    @Update("UPDATE task SET status = #{status}, completed_at = #{completedAt}, " +
            "updated_at = #{updatedAt} WHERE task_id = #{taskId}")
    int updateStatus(TaskPO po);

    /**
     * 查询指定日期所有重复实例的父任务ID（用于去重，不受完成状态影响）
     */
    @Select("SELECT repeat_parent_id FROM task WHERE user_id = #{userId} AND date = #{date} " +
            "AND is_repeat_instance = 1 AND repeat_parent_id IS NOT NULL AND deleted_at IS NULL")
    java.util.List<String> findExistingInstanceParentIds(@Param("userId") String userId, @Param("date") LocalDate date);

    /**
     * 更新任务信息（标题、优先级、日期、重复设置、重复结束日期）
     */
    @Update("UPDATE task SET title = #{title}, priority = #{priority}, date = #{date}, " +
            "repeat_type = #{repeatType}, repeat_config = #{repeatConfig}, " +
            "repeat_end_date = #{repeatEndDate}, updated_at = #{updatedAt} " +
            "WHERE task_id = #{taskId}")
    int updateTask(TaskPO po);

    /**
     * 逻辑删除任务
     */
    @Update("UPDATE task SET deleted_at = #{deletedAt}, updated_at = #{updatedAt} WHERE task_id = #{taskId}")
    int softDelete(TaskPO po);

    /**
     * 逻辑删除重复模板的所有关联实例
     */
    @Update("UPDATE task SET deleted_at = #{deletedAt}, updated_at = #{updatedAt} " +
            "WHERE repeat_parent_id = #{templateId} AND deleted_at IS NULL")
    int softDeleteByParentId(@Param("templateId") String templateId,
                              @Param("deletedAt") java.time.Instant deletedAt,
                              @Param("updatedAt") java.time.Instant updatedAt);

    /**
     * 插入带删除时间的任务记录（用于创建已删除实例）
     */
    @Insert("INSERT INTO task(task_id, user_id, title, priority, date, status, completed_at, " +
            "repeat_type, repeat_config, repeat_end_date, is_repeat_instance, repeat_parent_id, " +
            "created_at, updated_at, deleted_at) " +
            "VALUES(#{taskId}, #{userId}, #{title}, #{priority}, #{date}, #{status}, #{completedAt}, " +
            "#{repeatType}, #{repeatConfig}, #{repeatEndDate}, #{repeatInstance}, #{repeatParentId}, " +
            "#{createdAt}, #{updatedAt}, #{deletedAt})")
    int insertWithDeletedAt(TaskPO po);

    /**
     * 更新模板的重复结束日期
     */
    @Update("UPDATE task SET repeat_end_date = #{repeatEndDate}, updated_at = #{updatedAt} " +
            "WHERE task_id = #{taskId}")
    int updateRepeatEndDate(TaskPO po);

    /**
     * 逻辑删除指定模板在某日期及之后的所有实例
     */
    @Update("UPDATE task SET deleted_at = #{deletedAt}, updated_at = #{updatedAt} " +
            "WHERE repeat_parent_id = #{templateId} AND date >= #{fromDate} AND deleted_at IS NULL")
    int softDeleteInstancesFromDate(@Param("templateId") String templateId,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("deletedAt") java.time.Instant deletedAt,
                                     @Param("updatedAt") java.time.Instant updatedAt);
}
