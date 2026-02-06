package com.gxl.plancore.focus.infrastructure.persistence.repository;

import com.gxl.plancore.focus.domain.entity.FocusSession;
import com.gxl.plancore.focus.domain.repository.FocusSessionRepository;
import com.gxl.plancore.focus.domain.valueobject.SessionId;
import com.gxl.plancore.focus.infrastructure.persistence.converter.FocusSessionConverter;
import com.gxl.plancore.focus.infrastructure.persistence.mapper.FocusSessionMapper;
import com.gxl.plancore.focus.infrastructure.persistence.po.FocusSessionPO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 专注会话仓储实现
 */
@Repository
public class FocusSessionRepositoryImpl implements FocusSessionRepository {

    private final FocusSessionMapper focusSessionMapper;

    public FocusSessionRepositoryImpl(FocusSessionMapper focusSessionMapper) {
        this.focusSessionMapper = focusSessionMapper;
    }

    @Override
    public Optional<FocusSession> findBySessionId(SessionId sessionId) {
        FocusSessionPO po = focusSessionMapper.findBySessionId(sessionId.getValue());
        return Optional.ofNullable(FocusSessionConverter.toDomain(po));
    }

    @Override
    public Optional<FocusSession> findRunningSession(String userId) {
        FocusSessionPO po = focusSessionMapper.findRunningSession(userId);
        return Optional.ofNullable(FocusSessionConverter.toDomain(po));
    }

    @Override
    public void save(FocusSession session) {
        FocusSessionPO po = FocusSessionConverter.toPO(session);
        focusSessionMapper.insert(po);
    }

    @Override
    public void update(FocusSession session) {
        FocusSessionPO po = FocusSessionConverter.toPO(session);
        focusSessionMapper.update(po);
    }
}
