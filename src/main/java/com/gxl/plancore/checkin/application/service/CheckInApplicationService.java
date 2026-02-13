package com.gxl.plancore.checkin.application.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gxl.plancore.checkin.application.dto.CheckInResult;
import com.gxl.plancore.checkin.application.dto.CheckInStreakResult;
import com.gxl.plancore.checkin.domain.entity.CheckIn;
import com.gxl.plancore.checkin.domain.repository.CheckInRepository;
import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ErrorCode;

/**
 * 打卡应用服务
 * 编排打卡业务流程
 */
@Service
public class CheckInApplicationService {

    private static final Logger log = LoggerFactory.getLogger(CheckInApplicationService.class);

    private final CheckInRepository checkInRepository;

    public CheckInApplicationService(CheckInRepository checkInRepository) {
        this.checkInRepository = checkInRepository;
    }

    /**
     * 打卡
     * 幂等：同一天重复打卡返回已有记录
     *
     * @param userId  用户ID
     * @param dateStr 打卡日期 YYYY-MM-DD
     * @return 打卡结果
     */
    @Transactional
    public CheckInResult checkIn(String userId, String dateStr) {
        log.info("打卡: userId={}, date={}", userId, dateStr);

        // 1. 校验日期格式
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "日期格式必须为 YYYY-MM-DD");
        }

        // 2. 幂等检查：是否已打卡
        Optional<CheckIn> existingOpt = checkInRepository.findByUserIdAndDate(userId, dateStr);
        if (existingOpt.isPresent()) {
            CheckIn existing = existingOpt.get();
            log.info("当日已打卡，幂等返回: userId={}, date={}", userId, dateStr);
            return new CheckInResult(
                    existing.getDate().toString(),
                    existing.getCheckedAt(),
                    existing.getConsecutiveDays()
            );
        }

        // 3. 计算连续天数
        int consecutiveDays = calculateConsecutiveDays(userId, date);

        // 4. 创建打卡记录
        CheckIn checkIn = CheckIn.create(userId, date, consecutiveDays);
        checkInRepository.save(checkIn);

        // 5. 同步更新用户表的打卡统计（防腐层）
        checkInRepository.updateUserStreak(userId, consecutiveDays, dateStr);

        log.info("打卡成功: userId={}, date={}, consecutiveDays={}", userId, dateStr, consecutiveDays);

        return new CheckInResult(
                checkIn.getDate().toString(),
                checkIn.getCheckedAt(),
                checkIn.getConsecutiveDays()
        );
    }

    /**
     * 查询打卡连续天数
     *
     * @param userId 用户ID
     * @return 连续天数结果
     */
    public CheckInStreakResult queryStreak(String userId) {
        log.info("查询打卡连续天数: userId={}", userId);

        Optional<CheckIn> latestOpt = checkInRepository.findLatestByUserId(userId);

        // 无打卡记录
        if (latestOpt.isEmpty()) {
            return new CheckInStreakResult(0, null);
        }

        CheckIn latest = latestOpt.get();
        LocalDate today = LocalDate.now();
        LocalDate lastDate = latest.getDate();

        // 判断连续性是否还在
        // 最近打卡日是今天 → 连续天数有效
        // 最近打卡日是昨天 → 连续天数有效（今天还未打卡但未断签）
        // 最近打卡日早于昨天 → 已断签，连续天数为 0
        LocalDate yesterday = today.minusDays(1);

        int consecutiveDays;
        if (lastDate.equals(today) || lastDate.equals(yesterday)) {
            consecutiveDays = latest.getConsecutiveDays();
        } else {
            consecutiveDays = 0;
        }

        return new CheckInStreakResult(consecutiveDays, lastDate.toString());
    }

    /**
     * 计算本次打卡的连续天数
     *
     * @param userId 用户ID
     * @param date   打卡日期
     * @return 连续天数
     */
    private int calculateConsecutiveDays(String userId, LocalDate date) {
        // 查找前一天的打卡记录
        LocalDate yesterday = date.minusDays(1);
        Optional<CheckIn> yesterdayOpt = checkInRepository.findByUserIdAndDate(userId, yesterday.toString());

        if (yesterdayOpt.isPresent()) {
            // 前一天有打卡 → 连续天数 +1
            return yesterdayOpt.get().getConsecutiveDays() + 1;
        }

        // 前一天无打卡 → 从 1 开始
        return 1;
    }
}
