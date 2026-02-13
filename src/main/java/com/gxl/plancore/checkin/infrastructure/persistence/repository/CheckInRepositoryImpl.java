package com.gxl.plancore.checkin.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.gxl.plancore.checkin.domain.entity.CheckIn;
import com.gxl.plancore.checkin.domain.repository.CheckInRepository;
import com.gxl.plancore.checkin.infrastructure.persistence.converter.CheckInConverter;
import com.gxl.plancore.checkin.infrastructure.persistence.mapper.CheckInMapper;
import com.gxl.plancore.checkin.infrastructure.persistence.po.CheckInPO;

/**
 * 打卡仓储实现
 */
@Repository
public class CheckInRepositoryImpl implements CheckInRepository {

    private final CheckInMapper checkInMapper;

    public CheckInRepositoryImpl(CheckInMapper checkInMapper) {
        this.checkInMapper = checkInMapper;
    }

    @Override
    public void save(CheckIn checkIn) {
        CheckInPO po = CheckInConverter.toPO(checkIn);
        checkInMapper.insert(po);
    }

    @Override
    public Optional<CheckIn> findByUserIdAndDate(String userId, String date) {
        CheckInPO po = checkInMapper.findByUserIdAndDate(userId, date);
        return Optional.ofNullable(CheckInConverter.toDomain(po));
    }

    @Override
    public Optional<CheckIn> findLatestByUserId(String userId) {
        CheckInPO po = checkInMapper.findLatestByUserId(userId);
        return Optional.ofNullable(CheckInConverter.toDomain(po));
    }

    @Override
    public void updateUserStreak(String userId, int consecutiveDays, String lastCheckInDate) {
        checkInMapper.updateUserStreak(userId, consecutiveDays, lastCheckInDate);
    }
}
