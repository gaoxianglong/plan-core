package com.gxl.plancore.task.infrastructure.persistence.mapper;

import com.gxl.plancore.task.infrastructure.persistence.po.SubTaskPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 子任务 Mapper
 */
@Mapper
public interface SubTaskMapper {

    /**
     * 根据父任务ID查询子任务
     */
    @Select("SELECT id, sub_task_id, parent_task_id, user_id, title, status, completed_at, " +
            "repeat_type, repeat_config, is_repeat_instance, repeat_parent_id, created_at, updated_at " +
            "FROM sub_task WHERE parent_task_id = #{parentTaskId} AND deleted_at IS NULL " +
            "ORDER BY created_at ASC")
    List<SubTaskPO> findByParentTaskId(@Param("parentTaskId") String parentTaskId);

    /**
     * 根据父任务ID列表批量查询子任务
     */
    @Select("<script>" +
            "SELECT id, sub_task_id, parent_task_id, user_id, title, status, completed_at, " +
            "repeat_type, repeat_config, is_repeat_instance, repeat_parent_id, created_at, updated_at " +
            "FROM sub_task WHERE parent_task_id IN " +
            "<foreach collection='parentTaskIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "AND deleted_at IS NULL ORDER BY created_at ASC" +
            "</script>")
    List<SubTaskPO> findByParentTaskIds(@Param("parentTaskIds") List<String> parentTaskIds);

    /**
     * 统计父任务下未完成的子任务数量
     */
    @Select("SELECT COUNT(1) FROM sub_task WHERE parent_task_id = #{parentTaskId} " +
            "AND status = 'INCOMPLETE' AND deleted_at IS NULL")
    int countIncompleteByParentTaskId(@Param("parentTaskId") String parentTaskId);

    /**
     * 统计父任务下所有子任务数量（不含已删除）
     */
    @Select("SELECT COUNT(1) FROM sub_task WHERE parent_task_id = #{parentTaskId} " +
            "AND deleted_at IS NULL")
    int countByParentTaskId(@Param("parentTaskId") String parentTaskId);

    /**
     * 插入子任务记录
     */
    @Insert("INSERT INTO sub_task(sub_task_id, parent_task_id, user_id, title, status, completed_at, " +
            "repeat_type, repeat_config, is_repeat_instance, repeat_parent_id, " +
            "created_at, updated_at) " +
            "VALUES(#{subTaskId}, #{parentTaskId}, #{userId}, #{title}, #{status}, #{completedAt}, " +
            "#{repeatType}, #{repeatConfig}, #{repeatInstance}, #{repeatParentId}, " +
            "#{createdAt}, #{updatedAt})")
    int insert(SubTaskPO po);

    /**
     * 根据子任务ID查询子任务
     */
    @Select("SELECT id, sub_task_id, parent_task_id, user_id, title, status, completed_at, " +
            "repeat_type, repeat_config, is_repeat_instance, repeat_parent_id, created_at, updated_at " +
            "FROM sub_task WHERE sub_task_id = #{subTaskId} AND deleted_at IS NULL")
    SubTaskPO findBySubTaskId(@Param("subTaskId") String subTaskId);

    /**
     * 更新子任务信息（标题、重复设置）
     */
    @Update("UPDATE sub_task SET title = #{title}, repeat_type = #{repeatType}, " +
            "repeat_config = #{repeatConfig}, updated_at = #{updatedAt} " +
            "WHERE sub_task_id = #{subTaskId}")
    int updateSubTask(SubTaskPO po);

    /**
     * 逻辑删除子任务
     */
    @Update("UPDATE sub_task SET deleted_at = NOW(3), updated_at = NOW(3) " +
            "WHERE sub_task_id = #{subTaskId} AND deleted_at IS NULL")
    int softDelete(@Param("subTaskId") String subTaskId);

    /**
     * 更新子任务状态和完成时间
     */
    @Update("UPDATE sub_task SET status = #{status}, completed_at = #{completedAt}, " +
            "updated_at = #{updatedAt} WHERE sub_task_id = #{subTaskId}")
    int updateStatus(SubTaskPO po);
}
