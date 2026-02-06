package com.gxl.plancore.focus.infrastructure.persistence.converter;

import com.gxl.plancore.focus.domain.entity.FocusSession;
import com.gxl.plancore.focus.domain.valueobject.EndType;
import com.gxl.plancore.focus.domain.valueobject.FocusDuration;
import com.gxl.plancore.focus.domain.valueobject.FocusType;
import com.gxl.plancore.focus.domain.valueobject.SessionId;
import com.gxl.plancore.focus.domain.valueobject.SessionStatus;
import com.gxl.plancore.focus.infrastructure.persistence.po.FocusSessionPO;

/**
 * 专注会话 PO/领域对象转换器
 */
public class FocusSessionConverter {

    private FocusSessionConverter() {
    }

    /**
     * PO 转 领域对象
     */
    public static FocusSession toDomain(FocusSessionPO po) {
        if (po == null) {
            return null;
        }

        EndType endType = null;
        if (po.getEndType() != null && !po.getEndType().isEmpty()) {
            endType = EndType.valueOf(po.getEndType());
        }

        return FocusSession.reconstitute(
                SessionId.of(po.getSessionId()),
                po.getUserId(),
                FocusDuration.ofSeconds(po.getDurationSeconds()),
                FocusType.valueOf(po.getType()),
                SessionStatus.valueOf(po.getStatus()),
                po.getStartAt(),
                po.getEndAt(),
                po.getElapsedSeconds() != null ? po.getElapsedSeconds() : 0,
                endType,
                po.getCounted() != null && po.getCounted(),
                po.getCountedSeconds() != null ? po.getCountedSeconds() : 0,
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }

    /**
     * 领域对象 转 PO
     */
    public static FocusSessionPO toPO(FocusSession session) {
        if (session == null) {
            return null;
        }

        FocusSessionPO po = new FocusSessionPO();
        po.setSessionId(session.getSessionId().getValue());
        po.setUserId(session.getUserId());
        po.setDurationSeconds(session.getDuration().getSeconds());
        po.setType(session.getType().name());
        po.setStatus(session.getStatus().name());
        po.setStartAt(session.getStartAt());
        po.setEndAt(session.getEndAt());
        po.setElapsedSeconds(session.getElapsedSeconds());
        po.setEndType(session.getEndType() != null ? session.getEndType().name() : null);
        po.setCounted(session.isCounted());
        po.setCountedSeconds(session.getCountedSeconds());
        po.setCreatedAt(session.getCreatedAt());
        po.setUpdatedAt(session.getUpdatedAt());
        return po;
    }
}
