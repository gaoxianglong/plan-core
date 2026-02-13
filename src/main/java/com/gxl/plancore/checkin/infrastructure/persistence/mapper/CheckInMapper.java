package com.gxl.plancore.checkin.infrastructure.persistence.mapper;

import com.gxl.plancore.checkin.infrastructure.persistence.po.CheckInPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 打卡 MyBatis Mapper
 */
@Mapper
public interface CheckInMapper {

    String COLUMNS = "check_in_id, user_id, date, checked_at, consecutive_days, created_at, updated_at";

    /**
     * 插入打卡记录
     */
    @Insert("INSERT INTO check_in (check_in_id, user_id, date, checked_at, consecutive_days, created_at) " +
            "VALUES (#{checkInId}, #{userId}, #{date}, #{checkedAt}, #{consecutiveDays}, #{createdAt})")
    int insert(CheckInPO po);

    /**
     * 查询用户某天的打卡记录
     */
    @Select("SELECT " + COLUMNS + " FROM check_in " +
            "WHERE user_id = #{userId} AND date = #{date}")
    CheckInPO findByUserIdAndDate(@Param("userId") String userId, @Param("date") String date);

    /**
     * 查询用户最近一次打卡记录（按日期降序取第一条）
     */
    @Select("SELECT " + COLUMNS + " FROM check_in " +
            "WHERE user_id = #{userId} ORDER BY date DESC LIMIT 1")
    CheckInPO findLatestByUserId(@Param("userId") String userId);

    /**
     * 更新用户表的连续打卡天数和最近打卡日期
     * （防腐层适配：打卡上下文同步更新 user 表的打卡统计字段）
     */
    @Update("UPDATE `user` SET consecutive_days = #{consecutiveDays}, " +
            "last_check_in_date = #{lastCheckInDate} " +
            "WHERE user_id = #{userId}")
    int updateUserStreak(@Param("userId") String userId,
                         @Param("consecutiveDays") int consecutiveDays,
                         @Param("lastCheckInDate") String lastCheckInDate);
}
