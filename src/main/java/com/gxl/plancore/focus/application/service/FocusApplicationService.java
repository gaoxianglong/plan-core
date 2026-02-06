package com.gxl.plancore.focus.application.service;

import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.focus.application.command.EndFocusCommand;
import com.gxl.plancore.focus.application.command.StartFocusCommand;
import com.gxl.plancore.focus.application.dto.EndFocusResult;
import com.gxl.plancore.focus.application.dto.StartFocusResult;
import com.gxl.plancore.focus.domain.entity.FocusSession;
import com.gxl.plancore.focus.domain.entity.FocusStats;
import com.gxl.plancore.focus.domain.repository.FocusSessionRepository;
import com.gxl.plancore.focus.domain.repository.FocusStatsRepository;
import com.gxl.plancore.focus.domain.valueobject.EndType;
import com.gxl.plancore.focus.domain.valueobject.FocusDuration;
import com.gxl.plancore.focus.domain.valueobject.FocusType;
import com.gxl.plancore.focus.domain.valueobject.SessionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * 专注应用服务
 * 编排专注会话相关业务流程
 */
@Service
public class FocusApplicationService {

    private static final Logger log = LoggerFactory.getLogger(FocusApplicationService.class);

    private final FocusSessionRepository focusSessionRepository;
    private final FocusStatsRepository focusStatsRepository;

    public FocusApplicationService(FocusSessionRepository focusSessionRepository,
                                   FocusStatsRepository focusStatsRepository) {
        this.focusSessionRepository = focusSessionRepository;
        this.focusStatsRepository = focusStatsRepository;
    }

    /**
     * 开始专注
     *
     * @param command 开始专注命令
     * @return 专注会话结果
     */
    @Transactional
    public StartFocusResult startFocus(StartFocusCommand command) {
        log.info("开始专注: userId={}, durationSeconds={}, type={}",
                command.getUserId(), command.getDurationSeconds(), command.getType());

        // 1. 检查是否存在进行中的会话，如果有则自动结算
        Optional<FocusSession> runningSession = focusSessionRepository.findRunningSession(command.getUserId());
        if (runningSession.isPresent()) {
            autoSettleStaleSession(runningSession.get());
        }

        // 2. 校验时长
        FocusDuration duration;
        try {
            duration = FocusDuration.ofSeconds(command.getDurationSeconds());
        } catch (IllegalArgumentException e) {
            log.warn("开始专注失败: 时长无效, durationSeconds={}", command.getDurationSeconds());
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 3. 校验专注类型
        FocusType focusType;
        try {
            focusType = FocusType.valueOf(command.getType());
        } catch (IllegalArgumentException e) {
            log.warn("开始专注失败: 类型无效, type={}", command.getType());
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 4. 创建专注会话
        FocusSession session = FocusSession.start(command.getUserId(), duration, focusType);

        // 5. 持久化
        focusSessionRepository.save(session);

        log.info("开始专注成功: userId={}, sessionId={}, durationSeconds={}, type={}",
                command.getUserId(), session.getSessionId().getValue(),
                duration.getSeconds(), focusType.name());

        // 6. 返回结果
        return new StartFocusResult(
                session.getSessionId().getValue(),
                session.getDuration().getSeconds(),
                session.getType().name(),
                session.getStartAt(),
                session.getExpectedEndAt()
        );
    }

    /**
     * 结束专注
     *
     * 业务流程：
     * 1. 查询会话并校验状态
     * 2. 调用领域方法结束会话（含计入规则判断）
     * 3. 更新 focus_session 表
     * 4. 如果本次计入（counted=true），累加到 user_focus_stats 表
     * 5. 返回结果（包含累计总专注时长）
     *
     * @param command 结束专注命令
     * @return 结束专注结果
     */
    @Transactional
    public EndFocusResult endFocus(EndFocusCommand command) {
        log.info("结束专注: userId={}, sessionId={}, elapsedSeconds={}, endType={}",
                command.getUserId(), command.getSessionId(),
                command.getElapsedSeconds(), command.getEndType());

        // 1. 查询会话
        SessionId sessionId = SessionId.of(command.getSessionId());
        Optional<FocusSession> sessionOptional = focusSessionRepository.findBySessionId(sessionId);
        if (!sessionOptional.isPresent()) {
            log.warn("结束专注失败: 会话不存在, sessionId={}", command.getSessionId());
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        FocusSession session = sessionOptional.get();

        // 2. 校验会话归属
        if (!session.getUserId().equals(command.getUserId())) {
            log.warn("结束专注失败: 会话不属于当前用户, sessionId={}, sessionUserId={}, requestUserId={}",
                    command.getSessionId(), session.getUserId(), command.getUserId());
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 3. 幂等处理：如果会话已结束，直接返回已有结果
        if (session.isEnded()) {
            log.info("会话已结束（幂等）: sessionId={}", command.getSessionId());
            long totalFocusTime = getTotalFocusTime(command.getUserId());
            return new EndFocusResult(
                    session.getSessionId().getValue(),
                    session.isCounted(),
                    session.getCountedSeconds(),
                    totalFocusTime
            );
        }

        // 4. 校验结束类型
        EndType endType;
        try {
            endType = EndType.valueOf(command.getEndType());
        } catch (IllegalArgumentException e) {
            log.warn("结束专注失败: 结束类型无效, endType={}", command.getEndType());
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 5. 调用领域方法结束会话（包含计入规则判断）
        session.end(command.getElapsedSeconds(), endType);

        // 6. 更新 focus_session 表
        focusSessionRepository.update(session);

        // 7. 如果本次计入，累加到 user_focus_stats 表
        if (session.isCounted()) {
            updateFocusStats(command.getUserId(), session.getCountedSeconds());
        }

        // 8. 查询累计总专注时长
        long totalFocusTime = getTotalFocusTime(command.getUserId());

        log.info("结束专注成功: userId={}, sessionId={}, counted={}, countedSeconds={}, totalFocusTime={}",
                command.getUserId(), command.getSessionId(),
                session.isCounted(), session.getCountedSeconds(), totalFocusTime);

        return new EndFocusResult(
                session.getSessionId().getValue(),
                session.isCounted(),
                session.getCountedSeconds(),
                totalFocusTime
        );
    }

    /**
     * 自动结算残留的 RUNNING 会话
     *
     * 场景：前端进程意外退出（崩溃/杀进程），导致会话未正常结束
     * 策略：
     * - 已超过预计结束时间 → 按"自然结束"结算，计入全部时长（durationSeconds）
     * - 未超过预计结束时间 → 按"手动结束"结算，已完成秒数 = 实际经过时间，按 50% 规则判断是否计入
     */
    private void autoSettleStaleSession(FocusSession staleSession) {
        Instant now = Instant.now();
        Instant expectedEndAt = staleSession.getExpectedEndAt();

        if (now.isAfter(expectedEndAt) || now.equals(expectedEndAt)) {
            // 已超过预计结束时间 → 自然结束，计入全部预设时长
            int fullDuration = staleSession.getDuration().getSeconds();
            staleSession.end(fullDuration, EndType.NATURAL);
            log.info("自动结算残留会话（自然结束）: sessionId={}, countedSeconds={}",
                    staleSession.getSessionId().getValue(), fullDuration);
        } else {
            // 还在时间窗口内 → 手动结束，已完成秒数 = 从开始到现在的实际经过时间
            long elapsedSeconds = now.getEpochSecond() - staleSession.getStartAt().getEpochSecond();
            staleSession.end((int) elapsedSeconds, EndType.MANUAL);
            log.info("自动结算残留会话（手动结束）: sessionId={}, elapsedSeconds={}, counted={}",
                    staleSession.getSessionId().getValue(), elapsedSeconds, staleSession.isCounted());
        }

        // 更新 focus_session 表
        focusSessionRepository.update(staleSession);

        // 如果本次计入，累加到 user_focus_stats 表
        if (staleSession.isCounted()) {
            updateFocusStats(staleSession.getUserId(), staleSession.getCountedSeconds());
        }
    }

    /**
     * 更新用户专注统计（累加到 user_focus_stats）
     */
    private void updateFocusStats(String userId, int countedSeconds) {
        Optional<FocusStats> statsOptional = focusStatsRepository.findByUserId(userId);

        if (statsOptional.isPresent()) {
            // 已有统计记录，累加
            FocusStats stats = statsOptional.get();
            stats.addFocusTime(countedSeconds);
            focusStatsRepository.update(stats);
        } else {
            // 首次专注，创建统计记录
            FocusStats stats = FocusStats.create(userId);
            stats.addFocusTime(countedSeconds);
            focusStatsRepository.save(stats);
        }
    }

    /**
     * 查询用户累计总专注时间
     *
     * @param userId 用户ID
     * @return 专注统计，如果没有记录则返回空统计
     */
    public FocusStats queryTotalFocusTime(String userId) {
        log.info("查询总专注时间: userId={}", userId);
        Optional<FocusStats> statsOptional = focusStatsRepository.findByUserId(userId);
        if (statsOptional.isPresent()) {
            return statsOptional.get();
        }
        // 没有记录时返回空统计（0秒、0小时）
        return FocusStats.create(userId);
    }

    /**
     * 获取用户累计总专注时长（秒）- 内部方法
     */
    private long getTotalFocusTime(String userId) {
        Optional<FocusStats> statsOptional = focusStatsRepository.findByUserId(userId);
        if (statsOptional.isPresent()) {
            return statsOptional.get().getTotalSeconds();
        }
        return 0;
    }
}
