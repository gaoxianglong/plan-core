package com.gxl.plancore.focus.infrastructure.persistence.converter;

import com.gxl.plancore.focus.domain.entity.FocusStats;
import com.gxl.plancore.focus.infrastructure.persistence.po.FocusStatsPO;

/**
 * 专注统计 PO/领域对象转换器
 */
public class FocusStatsConverter {

    private FocusStatsConverter() {
    }

    /**
     * PO 转 领域对象
     */
    public static FocusStats toDomain(FocusStatsPO po) {
        if (po == null) {
            return null;
        }
        return FocusStats.reconstitute(
                po.getUserId(),
                po.getTotalSeconds() != null ? po.getTotalSeconds() : 0,
                po.getTotalHours() != null ? po.getTotalHours() : 0,
                po.getSessionCount() != null ? po.getSessionCount() : 0,
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }

    /**
     * 领域对象 转 PO
     */
    public static FocusStatsPO toPO(FocusStats stats) {
        if (stats == null) {
            return null;
        }
        FocusStatsPO po = new FocusStatsPO();
        po.setUserId(stats.getUserId());
        po.setTotalSeconds(stats.getTotalSeconds());
        po.setTotalHours(stats.getTotalHours());
        po.setSessionCount(stats.getSessionCount());
        po.setCreatedAt(stats.getCreatedAt());
        po.setUpdatedAt(stats.getUpdatedAt());
        return po;
    }
}
