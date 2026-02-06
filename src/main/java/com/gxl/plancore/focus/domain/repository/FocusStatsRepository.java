package com.gxl.plancore.focus.domain.repository;

import com.gxl.plancore.focus.domain.entity.FocusStats;

import java.util.Optional;

/**
 * 专注统计仓储接口
 * 领域层定义，由基础设施层实现
 */
public interface FocusStatsRepository {

    /**
     * 根据用户ID查询统计数据
     *
     * @param userId 用户ID
     * @return 专注统计
     */
    Optional<FocusStats> findByUserId(String userId);

    /**
     * 保存统计数据（新增）
     *
     * @param stats 专注统计实体
     */
    void save(FocusStats stats);

    /**
     * 更新统计数据
     *
     * @param stats 专注统计实体
     */
    void update(FocusStats stats);
}
