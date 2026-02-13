package com.gxl.plancore.checkin.domain.repository;

import java.util.Optional;

import com.gxl.plancore.checkin.domain.entity.CheckIn;

/**
 * 打卡仓储接口
 * 领域层定义，由基础设施层实现
 */
public interface CheckInRepository {

    /**
     * 保存打卡记录
     *
     * @param checkIn 打卡实体
     */
    void save(CheckIn checkIn);

    /**
     * 查询用户某天的打卡记录
     *
     * @param userId 用户ID
     * @param date   日期字符串 YYYY-MM-DD
     * @return 打卡记录
     */
    Optional<CheckIn> findByUserIdAndDate(String userId, String date);

    /**
     * 查询用户最近一次打卡记录
     *
     * @param userId 用户ID
     * @return 最近的打卡记录
     */
    Optional<CheckIn> findLatestByUserId(String userId);

    /**
     * 更新用户表的打卡连续天数和最近打卡日期
     * （防腐层：仅更新 user 表的 consecutive_days 和 last_check_in_date）
     *
     * @param userId          用户ID
     * @param consecutiveDays 连续天数
     * @param lastCheckInDate 最近打卡日期
     */
    void updateUserStreak(String userId, int consecutiveDays, String lastCheckInDate);
}
