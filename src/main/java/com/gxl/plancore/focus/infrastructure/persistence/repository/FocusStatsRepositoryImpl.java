package com.gxl.plancore.focus.infrastructure.persistence.repository;

import com.gxl.plancore.focus.domain.entity.FocusStats;
import com.gxl.plancore.focus.domain.repository.FocusStatsRepository;
import com.gxl.plancore.focus.infrastructure.persistence.converter.FocusStatsConverter;
import com.gxl.plancore.focus.infrastructure.persistence.mapper.FocusStatsMapper;
import com.gxl.plancore.focus.infrastructure.persistence.po.FocusStatsPO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 专注统计仓储实现
 */
@Repository
public class FocusStatsRepositoryImpl implements FocusStatsRepository {

    private final FocusStatsMapper focusStatsMapper;

    public FocusStatsRepositoryImpl(FocusStatsMapper focusStatsMapper) {
        this.focusStatsMapper = focusStatsMapper;
    }

    @Override
    public Optional<FocusStats> findByUserId(String userId) {
        FocusStatsPO po = focusStatsMapper.findByUserId(userId);
        return Optional.ofNullable(FocusStatsConverter.toDomain(po));
    }

    @Override
    public void save(FocusStats stats) {
        FocusStatsPO po = FocusStatsConverter.toPO(stats);
        focusStatsMapper.insert(po);
    }

    @Override
    public void update(FocusStats stats) {
        FocusStatsPO po = FocusStatsConverter.toPO(stats);
        focusStatsMapper.update(po);
    }
}
