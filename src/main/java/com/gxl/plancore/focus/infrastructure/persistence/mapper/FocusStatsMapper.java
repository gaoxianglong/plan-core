package com.gxl.plancore.focus.infrastructure.persistence.mapper;

import com.gxl.plancore.focus.infrastructure.persistence.po.FocusStatsPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户专注统计 MyBatis Mapper
 */
@Mapper
public interface FocusStatsMapper {

    @Select("SELECT id, user_id, total_seconds, total_hours, session_count, " +
            "created_at, updated_at " +
            "FROM user_focus_stats WHERE user_id = #{userId}")
    FocusStatsPO findByUserId(@Param("userId") String userId);

    @Insert("INSERT INTO user_focus_stats (user_id, total_seconds, total_hours, session_count, " +
            "created_at, updated_at) " +
            "VALUES (#{userId}, #{totalSeconds}, #{totalHours}, #{sessionCount}, " +
            "#{createdAt}, #{updatedAt})")
    int insert(FocusStatsPO po);

    @Update("UPDATE user_focus_stats SET total_seconds = #{totalSeconds}, " +
            "total_hours = #{totalHours}, session_count = #{sessionCount}, " +
            "updated_at = #{updatedAt} " +
            "WHERE user_id = #{userId}")
    int update(FocusStatsPO po);
}
