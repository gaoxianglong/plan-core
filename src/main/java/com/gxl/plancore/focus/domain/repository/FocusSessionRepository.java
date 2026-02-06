package com.gxl.plancore.focus.domain.repository;

import com.gxl.plancore.focus.domain.entity.FocusSession;
import com.gxl.plancore.focus.domain.valueobject.SessionId;

import java.util.Optional;

/**
 * 专注会话仓储接口
 * 领域层定义，由基础设施层实现
 */
public interface FocusSessionRepository {

    /**
     * 根据会话ID查询
     *
     * @param sessionId 会话ID
     * @return 专注会话
     */
    Optional<FocusSession> findBySessionId(SessionId sessionId);

    /**
     * 查询用户当前进行中的专注会话
     *
     * @param userId 用户ID
     * @return 进行中的会话，如果没有则返回 empty
     */
    Optional<FocusSession> findRunningSession(String userId);

    /**
     * 保存专注会话
     *
     * @param session 专注会话实体
     */
    void save(FocusSession session);

    /**
     * 更新专注会话
     *
     * @param session 专注会话实体
     */
    void update(FocusSession session);
}
