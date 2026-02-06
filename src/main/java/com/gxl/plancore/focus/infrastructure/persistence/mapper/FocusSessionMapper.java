package com.gxl.plancore.focus.infrastructure.persistence.mapper;

import com.gxl.plancore.focus.infrastructure.persistence.po.FocusSessionPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 专注会话 MyBatis Mapper
 */
@Mapper
public interface FocusSessionMapper {

    @Select("SELECT id, session_id, user_id, duration_seconds, type, status, " +
            "start_at, end_at, elapsed_seconds, end_type, counted, counted_seconds, " +
            "created_at, updated_at " +
            "FROM focus_session WHERE session_id = #{sessionId}")
    FocusSessionPO findBySessionId(@Param("sessionId") String sessionId);

    @Select("SELECT id, session_id, user_id, duration_seconds, type, status, " +
            "start_at, end_at, elapsed_seconds, end_type, counted, counted_seconds, " +
            "created_at, updated_at " +
            "FROM focus_session WHERE user_id = #{userId} AND status = 'RUNNING' " +
            "ORDER BY created_at DESC LIMIT 1")
    FocusSessionPO findRunningSession(@Param("userId") String userId);

    @Insert("INSERT INTO focus_session (session_id, user_id, duration_seconds, type, status, " +
            "start_at, end_at, elapsed_seconds, end_type, counted, counted_seconds, " +
            "created_at, updated_at) " +
            "VALUES (#{sessionId}, #{userId}, #{durationSeconds}, #{type}, #{status}, " +
            "#{startAt}, #{endAt}, #{elapsedSeconds}, #{endType}, #{counted}, #{countedSeconds}, " +
            "#{createdAt}, #{updatedAt})")
    int insert(FocusSessionPO po);

    @Update("UPDATE focus_session SET status = #{status}, end_at = #{endAt}, " +
            "elapsed_seconds = #{elapsedSeconds}, end_type = #{endType}, " +
            "counted = #{counted}, counted_seconds = #{countedSeconds}, " +
            "updated_at = #{updatedAt} " +
            "WHERE session_id = #{sessionId}")
    int update(FocusSessionPO po);
}
