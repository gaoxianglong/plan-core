package com.gxl.plancore.task.application.service;

import com.alibaba.fastjson2.JSON;
import com.gxl.plancore.common.exception.BusinessException;
import com.gxl.plancore.common.response.ErrorCode;
import com.gxl.plancore.task.application.command.CreateTaskCommand;
import com.gxl.plancore.task.application.dto.CreateTaskResult;
import com.gxl.plancore.task.application.dto.CreateSubTaskResult;
import com.gxl.plancore.task.application.dto.DeleteTaskResult;
import com.gxl.plancore.task.application.dto.ToggleSubTaskCompleteResult;
import com.gxl.plancore.task.application.dto.UpdateSubTaskResult;
import com.gxl.plancore.task.application.dto.MaterializeResult;
import com.gxl.plancore.task.application.dto.SubTaskDTO;
import com.gxl.plancore.task.application.dto.TaskListResult;
import com.gxl.plancore.task.application.dto.TaskWithSubTasksDTO;
import com.gxl.plancore.task.application.dto.ToggleCompleteResult;
import com.gxl.plancore.task.application.dto.UpdateTaskResult;
import com.gxl.plancore.task.domain.entity.SubTask;
import com.gxl.plancore.task.domain.entity.Task;
import com.gxl.plancore.task.domain.repository.SubTaskRepository;
import com.gxl.plancore.task.domain.repository.TaskRepository;
import com.gxl.plancore.task.domain.service.RepeatTaskMatcher;
import com.gxl.plancore.task.domain.service.VirtualTaskBuilder;
import com.gxl.plancore.task.domain.valueobject.RepeatType;
import com.gxl.plancore.task.domain.valueobject.TaskPriority;
import com.gxl.plancore.task.domain.valueobject.TaskStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务应用服务
 * 
 * 负责任务的创建、查询、重复任务虚拟展开与实例化
 */
@Service
public class TaskApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(TaskApplicationService.class);

    /** 单日任务数量上限 */
    private static final int DAILY_TASK_LIMIT = 50;

    /** 任务日期允许范围：今天 ±365 天 */
    private static final int DATE_RANGE_DAYS = 365;

    /** 幂等缓存：requestId -> taskId（进程内缓存，重启后失效） */
    private static final ConcurrentHashMap<String, String> REQUEST_TASK_CACHE = new ConcurrentHashMap<String, String>();

    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;

    /**
     * 构造任务应用服务
     */
    public TaskApplicationService(TaskRepository taskRepository, SubTaskRepository subTaskRepository) {
        this.taskRepository = taskRepository;
        this.subTaskRepository = subTaskRepository;
    }

    // ==========================================================================
    // 创建任务
    // ==========================================================================

    /**
     * 创建任务并返回结果
     * 
     * 执行流程：
     * 1. 幂等校验
     * 2. 参数校验（标题、日期、优先级、重复配置、结束日期）
     * 3. 业务规则（单日任务数上限）
     * 4. 创建任务并持久化
     * 5. 缓存幂等键
     */
    public CreateTaskResult createTask(CreateTaskCommand command) {
        log.info("开始创建任务: userId={}, title={}, date={}, repeatType={}",
                command.getUserId(), command.getTitle(), command.getDate(), command.getRepeatType());

        // ========== 1. 幂等校验 ==========
        String requestId = command.getRequestId();
        if (requestId != null && REQUEST_TASK_CACHE.containsKey(requestId)) {
            String existingTaskId = REQUEST_TASK_CACHE.get(requestId);
            Optional<Task> existing = taskRepository.findByTaskId(existingTaskId);
            if (existing.isPresent()) {
                log.info("幂等命中，返回已存在任务: requestId={}, taskId={}", requestId, existingTaskId);
                return buildCreateResult(existing.get());
            }
        }

        // ========== 2. 参数校验 ==========
        String title = normalizeTitle(command.getTitle());
        if (title.length() < 1 || title.length() > 100) {
            throw new BusinessException(ErrorCode.TASK_TITLE_INVALID);
        }

        LocalDate date = command.getDate();
        validateDateRange(date);

        TaskPriority priority = parsePriority(command.getPriority());
        RepeatType repeatType = parseRepeatType(command.getRepeatType());
        String repeatConfig = validateRepeatConfig(repeatType, command.getRepeatConfig());

        // 校验重复结束日期
        LocalDate repeatEndDate = command.getRepeatEndDate();
        if (repeatEndDate != null && repeatType == RepeatType.NONE) {
            throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
        }
        if (repeatEndDate != null && repeatEndDate.isBefore(date)) {
            throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
        }

        // ========== 3. 业务规则 ==========
        int count = taskRepository.countByUserIdAndDate(command.getUserId(), date);
        if (count >= DAILY_TASK_LIMIT) {
            throw new BusinessException(ErrorCode.TASK_DAILY_LIMIT);
        }

        // ========== 4. 创建任务并持久化 ==========
        Task task = Task.create(command.getUserId(), title, priority, date,
                repeatType, repeatConfig, repeatEndDate);
        taskRepository.save(task);
        log.info("任务创建成功: taskId={}, title={}, priority={}, repeatType={}",
                task.getTaskId(), task.getTitle(), priority, repeatType);

        // ========== 5. 缓存幂等键 ==========
        if (requestId != null) {
            REQUEST_TASK_CACHE.put(requestId, task.getTaskId());
            log.debug("幂等键已缓存: requestId={}, taskId={}", requestId, task.getTaskId());
        }

        return buildCreateResult(task);
    }

    // ==========================================================================
    // 实例化虚拟任务
    // ==========================================================================

    /**
     * 实例化虚拟任务
     * 
     * 将虚拟任务（格式 {模板ID}_{日期}）持久化为实例记录
     * 
     * @param virtualTaskId 虚拟任务 ID
     * @param userId 当前用户 ID（权限校验）
     * @return 实例化结果
     */
    public MaterializeResult materializeVirtualTask(String virtualTaskId, String userId) {
        log.info("开始实例化虚拟任务: virtualTaskId={}, userId={}", virtualTaskId, userId);

        // ========== 1. 解析虚拟 ID ==========
        VirtualIdInfo idInfo = parseVirtualTaskId(virtualTaskId);
        log.debug("虚拟ID解析完成: templateId={}, instanceDate={}", idInfo.templateId, idInfo.instanceDate);

        // ========== 2. 查询模板任务 ==========
        Optional<Task> templateOpt = taskRepository.findByTaskId(idInfo.templateId);
        if (!templateOpt.isPresent()) {
            throw new BusinessException(ErrorCode.TASK_PARENT_NOT_FOUND);
        }
        Task template = templateOpt.get();

        // 校验用户权限
        if (!template.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // ========== 3. 检查是否已存在实例 ==========
        List<String> parentIds = new ArrayList<String>();
        parentIds.add(idInfo.templateId);
        List<Task> existingInstances = taskRepository.findRepeatInstancesByDate(
                userId, idInfo.instanceDate, parentIds);
        if (!existingInstances.isEmpty()) {
            // 已有实例，直接返回（幂等）
            Task existing = existingInstances.get(0);
            log.info("实例已存在，直接返回: instanceTaskId={}, date={}", existing.getTaskId(), idInfo.instanceDate);
            return buildMaterializeResult(existing);
        }

        // ========== 4. 验证模板在该日期是否匹配 ==========
        if (!RepeatTaskMatcher.matches(template, idInfo.instanceDate)) {
            throw new BusinessException(ErrorCode.TASK_VIRTUAL_ID_INVALID);
        }

        // ========== 5. 创建实例 ==========
        Task instance = Task.createInstance(template, idInfo.instanceDate);
        taskRepository.save(instance);
        log.info("虚拟任务实例化成功: instanceTaskId={}, templateId={}, date={}",
                instance.getTaskId(), template.getTaskId(), idInfo.instanceDate);

        return buildMaterializeResult(instance);
    }

    // ==========================================================================
    // 完成/反完成任务
    // ==========================================================================

    /**
     * 完成或反完成任务
     * 
     * 支持普通任务和虚拟任务，虚拟任务会自动实例化后再更新状态。
     * 
     * @param taskId 任务 ID（支持虚拟 ID）
     * @param userId 当前用户 ID
     * @param completed true=完成，false=反完成
     * @return 操作结果
     */
    public ToggleCompleteResult toggleComplete(String taskId, String userId, boolean completed) {
        log.info("开始{}任务: taskId={}, userId={}", completed ? "完成" : "反完成", taskId, userId);

        // ========== 1. 判断是否为虚拟任务并获取真实任务 ==========
        Task task = resolveTask(taskId, userId);
        log.debug("任务解析完成: realTaskId={}, isRepeatInstance={}", task.getTaskId(), task.isRepeatInstance());

        // ========== 2. 完成时检查是否有未完成子任务 ==========
        if (completed) {
            // 确定检查子任务的父任务 ID
            String parentIdForSubTasks = task.getTaskId();
            if (task.isRepeatInstance() && task.getRepeatParentId() != null) {
                // 实例任务继承模板的子任务，检查模板的子任务
                parentIdForSubTasks = task.getRepeatParentId();
            }

            int incompleteSubTaskCount = subTaskRepository.countIncompleteByParentTaskId(parentIdForSubTasks);
            log.debug("检查未完成子任务: parentId={}, incompleteCount={}", parentIdForSubTasks, incompleteSubTaskCount);
            if (incompleteSubTaskCount > 0) {
                log.warn("任务存在未完成子任务，无法完成: taskId={}, incompleteCount={}", task.getTaskId(), incompleteSubTaskCount);
                throw new BusinessException(ErrorCode.TASK_HAS_INCOMPLETE_SUBTASKS);
            }
        }

        // ========== 3. 更新任务状态 ==========
        if (completed) {
            task.complete();
        } else {
            task.uncomplete();
        }
        taskRepository.update(task);
        log.info("任务状态更新成功: taskId={}, status={}, completedAt={}",
                task.getTaskId(), task.getStatus(), task.getCompletedAt());

        return new ToggleCompleteResult(
                task.getTaskId(),
                task.getStatus().name(),
                task.getCompletedAt()
        );
    }

    // ==========================================================================
    // 更新任务
    // ==========================================================================

    /**
     * 更新任务信息
     * 
     * 支持普通任务和虚拟任务，虚拟任务会自动实例化后再更新。
     * 
     * @param taskId 任务 ID（支持虚拟 ID）
     * @param userId 当前用户 ID
     * @param title 新标题（可选）
     * @param priority 新优先级（可选）
     * @param date 新日期（可选）
     * @param repeatType 新重复类型（可选）
     * @param repeatConfig 新重复配置（可选）
     * @return 更新结果
     */
    public UpdateTaskResult updateTask(String taskId, String userId, String title,
                                        String priority, LocalDate date,
                                        String repeatType, Map<String, Object> repeatConfig) {
        log.info("开始更新任务: taskId={}, userId={}", taskId, userId);

        // ========== 分支：是否修改重复设置 ==========
        if (repeatType != null) {
            // 修改重复设置走专用逻辑（支持模板/虚拟/实例ID）
            return updateWithRepeatChange(taskId, userId, title, priority, date, repeatType, repeatConfig);
        }

        // ========== 普通更新（不涉及重复设置） ==========
        Task task = resolveTask(taskId, userId);
        log.debug("任务解析完成: realTaskId={}", task.getTaskId());

        boolean updated = false;

        // 更新标题
        if (title != null) {
            String normalizedTitle = title.trim();
            if (normalizedTitle.length() < 1 || normalizedTitle.length() > 100) {
                throw new BusinessException(ErrorCode.TASK_TITLE_INVALID);
            }
            task.updateTitle(normalizedTitle);
            updated = true;
            log.debug("更新标题: {}", normalizedTitle);
        }

        // 更新优先级
        if (priority != null) {
            TaskPriority newPriority = parsePriority(priority);
            task.updatePriority(newPriority);
            updated = true;
            log.debug("更新优先级: {}", newPriority);
        }

        // 更新日期
        if (date != null) {
            validateDateRange(date);
            task.updateDate(date);
            updated = true;
            log.debug("更新日期: {}", date);
        }

        // 持久化
        if (updated) {
            taskRepository.updateTask(task);
            log.info("任务更新成功: taskId={}", task.getTaskId());
        } else {
            log.info("任务无更新: taskId={}", task.getTaskId());
        }

        return new UpdateTaskResult(
                task.getTaskId(),
                task.getTitle(),
                task.getPriority().name(),
                task.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                task.getRepeatType().name(),
                task.getRepeatConfig(),
                task.getUpdatedAt()
        );
    }

    /**
     * 修改重复设置（支持模板ID、虚拟ID、实例ID）
     * 
     * 核心逻辑：
     * 1. 无论传入哪种 ID，最终定位到模板任务进行修改
     * 2. 模板的 date 更新为生效日期（新重复规则从此日期开始，只影响未来）
     * 3. 清理生效日期及之后的已实例化任务（它们将按新规则重新生成虚拟任务）
     * 
     * 场景举例：
     * - 非重复 → 重复：模板变为重复模板，从生效日期开始每天/每周/每月重复
     * - DAILY → WEEKLY：日期改为今天，旧的未来实例被清理，按新周规则生成虚拟任务
     * - 重复 → 非重复：模板变为普通任务，不再产生虚拟任务
     */
    private UpdateTaskResult updateWithRepeatChange(String taskId, String userId,
                                                     String title, String priority, LocalDate date,
                                                     String repeatType, Map<String, Object> repeatConfig) {
        log.info("修改重复设置: taskId={}, newRepeatType={}", taskId, repeatType);

        // ========== 1. 定位模板任务 ==========
        Task template;
        LocalDate effectiveDate = LocalDate.now();

        if (isVirtualTaskId(taskId)) {
            // 虚拟任务 ID → 提取模板 ID，不做实例化
            VirtualIdInfo idInfo = parseVirtualTaskId(taskId);
            log.debug("虚拟任务ID，提取模板: templateId={}, date={}", idInfo.templateId, idInfo.instanceDate);

            Optional<Task> templateOpt = taskRepository.findByTaskId(idInfo.templateId);
            if (!templateOpt.isPresent()) {
                log.warn("虚拟任务的模板不存在: templateId={}", idInfo.templateId);
                throw new BusinessException(ErrorCode.TASK_PARENT_NOT_FOUND);
            }
            template = templateOpt.get();
            if (!template.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
            }
            effectiveDate = idInfo.instanceDate;

        } else {
            // 普通 ID（模板任务或实例任务）
            Optional<Task> taskOpt = taskRepository.findByTaskId(taskId);
            if (!taskOpt.isPresent()) {
                log.warn("任务不存在: taskId={}", taskId);
                throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
            }
            Task task = taskOpt.get();
            if (!task.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
            }

            if (task.isRepeatInstance()) {
                // 实例任务 → 找到父模板
                String parentId = task.getRepeatParentId();
                log.debug("实例任务，查找父模板: instanceId={}, parentId={}", taskId, parentId);

                Optional<Task> parentOpt = taskRepository.findByTaskId(parentId);
                if (!parentOpt.isPresent()) {
                    log.warn("实例任务的父模板不存在: parentId={}", parentId);
                    throw new BusinessException(ErrorCode.TASK_PARENT_NOT_FOUND);
                }
                template = parentOpt.get();
                effectiveDate = task.getDate();
            } else {
                // 模板任务或普通任务 → 直接操作
                template = task;
            }
        }

        log.info("定位到模板任务: templateId={}, 原repeatType={}, 生效日期={}",
                template.getTaskId(), template.getRepeatType(), effectiveDate);

        // ========== 2. 如果请求中有显式日期参数，使用它作为生效日期 ==========
        if (date != null) {
            validateDateRange(date);
            effectiveDate = date;
            log.debug("使用请求中的日期作为生效日期: {}", effectiveDate);
        }

        // ========== 3. 校验新的重复设置 ==========
        RepeatType newRepeatType = parseRepeatType(repeatType);
        String newRepeatConfig = validateRepeatConfig(newRepeatType, repeatConfig);
        log.debug("新重复设置校验通过: type={}, config={}", newRepeatType, newRepeatConfig);

        // ========== 4. 清理生效日期及之后的已实例化任务 ==========
        // 无论新旧 repeatType 是什么，都需要清理旧实例，让它们按新规则重新生成
        int cleanedCount = taskRepository.softDeleteInstancesFromDate(template.getTaskId(), effectiveDate);
        log.info("已清理生效日期之后的旧实例: templateId={}, fromDate={}, cleanedCount={}",
                template.getTaskId(), effectiveDate, cleanedCount);

        // ========== 5. 更新模板的重复设置和日期 ==========
        template.updateDate(effectiveDate);
        template.updateRepeat(newRepeatType, newRepeatConfig);

        // 如果改为非重复，清除重复结束日期
        if (newRepeatType == RepeatType.NONE) {
            template.updateRepeatEndDate(null);
            log.debug("改为非重复，清除 repeatEndDate");
        }

        // ========== 6. 同时更新其他字段（如果有） ==========
        if (title != null) {
            String normalizedTitle = title.trim();
            if (normalizedTitle.length() < 1 || normalizedTitle.length() > 100) {
                throw new BusinessException(ErrorCode.TASK_TITLE_INVALID);
            }
            template.updateTitle(normalizedTitle);
            log.debug("同时更新标题: {}", normalizedTitle);
        }
        if (priority != null) {
            TaskPriority newPriority = parsePriority(priority);
            template.updatePriority(newPriority);
            log.debug("同时更新优先级: {}", newPriority);
        }

        // ========== 7. 持久化 ==========
        taskRepository.updateTask(template);
        log.info("重复设置修改成功: templateId={}, newRepeatType={}, effectiveDate={}",
                template.getTaskId(), newRepeatType, effectiveDate);

        return new UpdateTaskResult(
                template.getTaskId(),
                template.getTitle(),
                template.getPriority().name(),
                template.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                template.getRepeatType().name(),
                template.getRepeatConfig(),
                template.getUpdatedAt()
        );
    }

    // ==========================================================================
    // 删除任务
    // ==========================================================================

    /**
     * 删除任务
     * 
     * 支持两种删除方式：
     * - deleteAll = false：仅删除指定日期的任务
     * - deleteAll = true：删除目标日期及之后的所有（不影响之前）
     * 
     * @param taskId 任务 ID（支持虚拟 ID）
     * @param userId 当前用户 ID
     * @param deleteAll 是否删除目标日期及之后的所有
     * @return 删除结果
     */
    public DeleteTaskResult deleteTask(String taskId, String userId, boolean deleteAll) {
        log.info("开始删除任务: taskId={}, userId={}, deleteAll={}", taskId, userId, deleteAll);

        int deletedCount = 0;

        // ========== 1. 判断是否为虚拟任务 ID ==========
        if (isVirtualTaskId(taskId)) {
            log.debug("检测到虚拟任务ID: {}", taskId);
            deletedCount = deleteVirtualTask(taskId, userId, deleteAll);
        } else {
            // ========== 2. 普通任务 ID ==========
            deletedCount = deleteRealTask(taskId, userId, deleteAll);
        }

        log.info("任务删除完成: taskId={}, deletedCount={}", taskId, deletedCount);
        return new DeleteTaskResult(deletedCount);
    }

    /**
     * 删除虚拟任务
     */
    private int deleteVirtualTask(String virtualTaskId, String userId, boolean deleteAll) {
        // 解析虚拟 ID
        int lastUnderscore = virtualTaskId.lastIndexOf('_');
        String templateId = virtualTaskId.substring(0, lastUnderscore);
        String dateStr = virtualTaskId.substring(lastUnderscore + 1);
        LocalDate instanceDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);

        log.debug("解析虚拟任务ID: templateId={}, date={}", templateId, instanceDate);

        // 查找模板任务
        Optional<Task> templateOpt = taskRepository.findByTaskId(templateId);
        if (!templateOpt.isPresent()) {
            log.warn("虚拟任务的模板不存在: templateId={}", templateId);
            // 幂等：模板不存在，视为已删除
            return 1;
        }
        Task template = templateOpt.get();

        // 校验用户权限
        if (!template.getUserId().equals(userId)) {
            log.warn("用户无权访问此任务: templateId={}, taskOwner={}, requestUser={}",
                    templateId, template.getUserId(), userId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        if (deleteAll) {
            // 删除目标日期及之后的所有：设置模板的 repeatEndDate 为目标日期的前一天
            return deleteFromDate(template, instanceDate);
        } else {
            // 仅删除该日期的虚拟任务：创建一条已删除的实例
            Task deletedInstance = Task.createDeletedInstance(template, instanceDate);
            taskRepository.saveDeletedInstance(deletedInstance);
            log.info("已创建删除标记实例: instanceId={}, date={}", deletedInstance.getTaskId(), instanceDate);
            return 1;
        }
    }

    /**
     * 删除真实任务（普通任务或实例任务）
     */
    private int deleteRealTask(String taskId, String userId, boolean deleteAll) {
        Optional<Task> taskOpt = taskRepository.findByTaskId(taskId);
        if (!taskOpt.isPresent()) {
            log.debug("任务不存在，视为已删除: taskId={}", taskId);
            // 幂等：任务不存在，视为已删除
            return 1;
        }
        Task task = taskOpt.get();

        // 校验用户权限
        if (!task.getUserId().equals(userId)) {
            log.warn("用户无权访问此任务: taskId={}, taskOwner={}, requestUser={}",
                    taskId, task.getUserId(), userId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 判断是否为重复实例任务
        if (deleteAll && task.isRepeatInstance()) {
            // 查找父模板
            String parentId = task.getRepeatParentId();
            Optional<Task> parentOpt = taskRepository.findByTaskId(parentId);
            if (parentOpt.isPresent()) {
                Task parent = parentOpt.get();
                // 删除该日期及之后的所有
                int count = deleteFromDate(parent, task.getDate());
                // 同时删除当前实例
                task.delete();
                taskRepository.softDelete(task);
                return count;
            }
        }

        // 判断是否为重复模板任务
        if (task.isRepeatTemplate()) {
            if (deleteAll) {
                // 对于模板任务，deleteAll 时删除模板及所有实例
                return deleteTemplateAndAllInstances(task);
            } else {
                // 仅删除当天：创建一个当天的"已删除实例"标记，不删除模板本身
                // 这样后续日期的虚拟任务仍然可以正常显示
                Task deletedInstance = Task.createDeletedInstance(task, task.getDate());
                taskRepository.saveDeletedInstance(deletedInstance);
                log.info("已创建删除标记实例（模板起始日）: instanceId={}, date={}", 
                        deletedInstance.getTaskId(), task.getDate());
                return 1;
            }
        }

        // 普通任务删除：逻辑删除该任务
        task.delete();
        taskRepository.softDelete(task);
        log.info("任务已删除: taskId={}", taskId);
        return 1;
    }

    /**
     * 删除目标日期及之后的所有（设置模板的 repeatEndDate）
     */
    private int deleteFromDate(Task template, LocalDate fromDate) {
        log.debug("删除目标日期及之后的所有: templateId={}, fromDate={}", template.getTaskId(), fromDate);

        // 设置模板的 repeatEndDate 为目标日期的前一天
        LocalDate newEndDate = fromDate.minusDays(1);
        template.updateRepeatEndDate(newEndDate);
        taskRepository.updateRepeatEndDate(template);
        log.info("已更新模板结束日期: templateId={}, newEndDate={}", template.getTaskId(), newEndDate);

        // 删除该日期及之后的所有已实例化的任务
        int instanceCount = taskRepository.softDeleteInstancesFromDate(template.getTaskId(), fromDate);
        log.debug("已删除实例数量: {}", instanceCount);

        return 1 + instanceCount;
    }

    /**
     * 删除模板及所有关联实例
     */
    private int deleteTemplateAndAllInstances(Task template) {
        log.debug("删除模板及所有实例: templateId={}", template.getTaskId());

        // 删除所有实例
        int instanceCount = taskRepository.softDeleteByParentId(template.getTaskId());
        log.debug("已删除实例数量: {}", instanceCount);

        // 删除模板
        template.delete();
        taskRepository.softDelete(template);
        log.info("模板已删除: templateId={}", template.getTaskId());

        return 1 + instanceCount;
    }

    /**
     * 解析任务 ID，返回真实任务
     * 
     * 如果是虚拟 ID，自动实例化后返回实例任务；
     * 如果是普通 ID，直接查询返回。
     */
    private Task resolveTask(String taskId, String userId) {
        // 尝试解析为虚拟 ID
        if (isVirtualTaskId(taskId)) {
            log.debug("检测到虚拟任务ID，开始实例化: virtualTaskId={}", taskId);
            MaterializeResult result = materializeVirtualTask(taskId, userId);
            Optional<Task> taskOpt = taskRepository.findByTaskId(result.getTaskId());
            if (!taskOpt.isPresent()) {
                log.error("实例化后任务查询失败: instanceTaskId={}", result.getTaskId());
                throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
            }
            return taskOpt.get();
        }

        // 普通任务 ID
        log.debug("普通任务ID查询: taskId={}", taskId);
        Optional<Task> taskOpt = taskRepository.findByTaskId(taskId);
        if (!taskOpt.isPresent()) {
            log.warn("任务不存在: taskId={}", taskId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        Task task = taskOpt.get();

        // 校验用户权限
        if (!task.getUserId().equals(userId)) {
            log.warn("用户无权访问此任务: taskId={}, taskOwner={}, requestUser={}", taskId, task.getUserId(), userId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        return task;
    }

    // ==========================================================================
    // 重复任务子任务按日隔离（Copy-on-Write）
    // ==========================================================================

    /**
     * 解析子任务，处理重复任务的按日隔离
     * 
     * 如果子任务属于重复模板且提供了日期：
     * 1. 自动实例化该日期的父任务
     * 2. 克隆模板子任务到实例（如果尚未克隆）
     * 3. 返回实例中对应的子任务
     * 
     * 如果子任务属于普通任务或实例任务，直接返回
     * 
     * @param subTask 原始子任务
     * @param date 操作日期（null 则直接操作原始子任务）
     * @param userId 用户 ID
     * @return 应操作的子任务（可能是原始的，也可能是实例副本）
     */
    private SubTask resolveSubTaskForDate(SubTask subTask, LocalDate date, String userId) {
        if (date == null) {
            return subTask;
        }

        // 查询父任务，判断是否为重复模板
        Optional<Task> parentOpt = taskRepository.findByTaskId(subTask.getParentTaskId());
        if (!parentOpt.isPresent()) {
            return subTask;
        }
        Task parentTask = parentOpt.get();

        // 非重复模板任务，直接操作
        if (!parentTask.isRepeatTemplate()) {
            return subTask;
        }

        log.info("重复任务子任务按日隔离: subTaskId={}, templateId={}, date={}",
                subTask.getSubTaskId(), parentTask.getTaskId(), date);

        // 构造虚拟 ID 并实例化父任务
        String virtualId = parentTask.getTaskId() + "_" + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Task instance = resolveTask(virtualId, userId);

        // 克隆模板子任务到实例（如果尚未克隆）
        return cloneAndFindSubTask(subTask, instance, parentTask.getTaskId());
    }

    /**
     * 克隆模板子任务到实例任务，并返回对应的实例副本
     * 
     * @param templateSubTask 模板子任务
     * @param instance 实例父任务
     * @param templateId 模板任务 ID
     * @return 实例中对应的子任务
     */
    private SubTask cloneAndFindSubTask(SubTask templateSubTask, Task instance, String templateId) {
        List<SubTask> instanceSubTasks = subTaskRepository.findByParentTaskId(instance.getTaskId());

        if (instanceSubTasks.isEmpty()) {
            // 克隆所有模板子任务到实例
            List<SubTask> templateSubTasks = subTaskRepository.findByParentTaskId(templateId);
            log.debug("克隆模板子任务到实例: templateId={}, instanceId={}, count={}",
                    templateId, instance.getTaskId(), templateSubTasks.size());

            for (SubTask tSub : templateSubTasks) {
                SubTask iSub = SubTask.createInstance(tSub, instance.getTaskId());
                subTaskRepository.save(iSub);
            }

            // 重新查询实例子任务
            instanceSubTasks = subTaskRepository.findByParentTaskId(instance.getTaskId());
        }

        // 查找与模板子任务对应的实例副本（通过 repeatParentId 匹配）
        for (SubTask iSub : instanceSubTasks) {
            if (templateSubTask.getSubTaskId().equals(iSub.getRepeatParentId())) {
                log.debug("找到实例子任务副本: templateSubTaskId={}, instanceSubTaskId={}",
                        templateSubTask.getSubTaskId(), iSub.getSubTaskId());
                return iSub;
            }
        }

        // 没有匹配的副本（不应该发生），返回原始子任务
        log.warn("未找到实例子任务副本: templateSubTaskId={}, instanceId={}",
                templateSubTask.getSubTaskId(), instance.getTaskId());
        return templateSubTask;
    }

    /**
     * 判断是否为虚拟任务 ID（包含下划线和日期格式）
     */
    private boolean isVirtualTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return false;
        }
        int lastUnderscore = taskId.lastIndexOf('_');
        if (lastUnderscore < 1) {
            return false;
        }
        String dateStr = taskId.substring(lastUnderscore + 1);
        // 简单判断日期格式 YYYY-MM-DD
        return dateStr.length() == 10 && dateStr.charAt(4) == '-' && dateStr.charAt(7) == '-';
    }

    // ==========================================================================
    // 创建子任务
    // ==========================================================================

    /** 单个父任务下子任务数量上限 */
    private static final int SUBTASK_LIMIT = 20;

    /**
     * 创建子任务
     * 
     * 执行流程：
     * 1. 校验父任务存在且属于当前用户
     * 2. 校验子任务数量上限
     * 3. 校验标题
     * 4. 处理重复设置（默认继承父任务）
     * 5. 创建子任务并持久化
     * 
     * @param parentTaskId 父任务 ID（支持虚拟 ID，自动提取模板 ID）
     * @param userId 当前用户 ID
     * @param requestId 幂等键
     * @param title 子任务标题
     * @param repeatType 重复类型（null 表示继承父任务）
     * @param repeatConfig 重复配置（null 表示继承父任务）
     * @return 创建结果
     */
    public CreateSubTaskResult createSubTask(String parentTaskId, String userId, String requestId,
                                              String title, String repeatType,
                                              Map<String, Object> repeatConfig) {
        log.info("开始创建子任务: parentTaskId={}, userId={}, title={}", parentTaskId, userId, title);

        // ========== 1. 幂等校验 ==========
        if (requestId != null && REQUEST_TASK_CACHE.containsKey(requestId)) {
            String existingSubTaskId = REQUEST_TASK_CACHE.get(requestId);
            log.info("幂等命中，返回已存在子任务: requestId={}, subTaskId={}", requestId, existingSubTaskId);
            // 幂等命中时需要从数据库查回完整数据
            List<SubTask> existing = subTaskRepository.findByParentTaskId(parentTaskId);
            for (SubTask st : existing) {
                if (st.getSubTaskId().equals(existingSubTaskId)) {
                    return buildCreateSubTaskResult(st);
                }
            }
        }

        // ========== 2. 解析父任务 ID ==========
        // 如果是虚拟任务 ID，先实例化父任务（子任务挂在实例上，只影响当天）
        String resolvedParentId = parentTaskId;
        if (isVirtualTaskId(parentTaskId)) {
            log.debug("虚拟任务ID，实例化父任务: {}", parentTaskId);
            Task instance = resolveTask(parentTaskId, userId);
            resolvedParentId = instance.getTaskId();

            // 克隆模板子任务到实例（如果尚未克隆）
            if (instance.isRepeatInstance() && instance.getRepeatParentId() != null) {
                List<SubTask> instanceSubTasks = subTaskRepository.findByParentTaskId(resolvedParentId);
                if (instanceSubTasks.isEmpty()) {
                    List<SubTask> templateSubTasks = subTaskRepository.findByParentTaskId(instance.getRepeatParentId());
                    log.debug("克隆模板子任务到实例: templateId={}, instanceId={}, count={}",
                            instance.getRepeatParentId(), resolvedParentId, templateSubTasks.size());
                    for (SubTask tSub : templateSubTasks) {
                        SubTask iSub = SubTask.createInstance(tSub, resolvedParentId);
                        subTaskRepository.save(iSub);
                    }
                }
            }
        }

        // ========== 3. 校验父任务存在 ==========
        Optional<Task> parentOpt = taskRepository.findByTaskId(resolvedParentId);
        if (!parentOpt.isPresent()) {
            log.warn("父任务不存在: parentTaskId={}", resolvedParentId);
            throw new BusinessException(ErrorCode.TASK_PARENT_NOT_FOUND);
        }
        Task parentTask = parentOpt.get();

        // 校验用户权限
        if (!parentTask.getUserId().equals(userId)) {
            log.warn("用户无权访问父任务: parentTaskId={}, taskOwner={}, requestUser={}",
                    resolvedParentId, parentTask.getUserId(), userId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // ========== 4. 校验子任务数量上限 ==========
        int currentCount = subTaskRepository.countByParentTaskId(resolvedParentId);
        if (currentCount >= SUBTASK_LIMIT) {
            log.warn("子任务数量超出上限: parentTaskId={}, currentCount={}, limit={}",
                    resolvedParentId, currentCount, SUBTASK_LIMIT);
            throw new BusinessException(ErrorCode.TASK_SUBTASK_LIMIT);
        }

        // ========== 5. 校验标题 ==========
        String normalizedTitle = title.trim();
        if (normalizedTitle.length() < 1 || normalizedTitle.length() > 50) {
            throw new BusinessException(ErrorCode.TASK_TITLE_INVALID);
        }

        // ========== 6. 处理重复设置 ==========
        RepeatType resolvedRepeatType;
        String resolvedRepeatConfig;

        if (repeatType != null) {
            // 用户显式指定重复设置
            resolvedRepeatType = parseRepeatType(repeatType);
            resolvedRepeatConfig = validateRepeatConfig(resolvedRepeatType, repeatConfig);
            log.debug("使用用户指定的重复设置: type={}, config={}", resolvedRepeatType, resolvedRepeatConfig);
        } else {
            // 默认继承父任务的重复设置
            resolvedRepeatType = parentTask.getRepeatType();
            resolvedRepeatConfig = parentTask.getRepeatConfig();
            log.debug("继承父任务的重复设置: type={}, config={}", resolvedRepeatType, resolvedRepeatConfig);
        }

        // ========== 7. 创建子任务并持久化 ==========
        SubTask subTask = SubTask.create(resolvedParentId, userId, normalizedTitle,
                resolvedRepeatType, resolvedRepeatConfig);
        subTaskRepository.save(subTask);
        log.info("子任务创建成功: subTaskId={}, parentTaskId={}, title={}, repeatType={}",
                subTask.getSubTaskId(), resolvedParentId, normalizedTitle, resolvedRepeatType);

        // ========== 8. 缓存幂等键 ==========
        if (requestId != null) {
            REQUEST_TASK_CACHE.put(requestId, subTask.getSubTaskId());
            log.debug("幂等键已缓存: requestId={}, subTaskId={}", requestId, subTask.getSubTaskId());
        }

        return buildCreateSubTaskResult(subTask);
    }

    /**
     * 组装创建子任务结果
     */
    private CreateSubTaskResult buildCreateSubTaskResult(SubTask subTask) {
        return new CreateSubTaskResult(
                subTask.getSubTaskId(),
                subTask.getParentTaskId(),
                subTask.getTitle(),
                subTask.getStatus().name(),
                subTask.getRepeatType().name(),
                subTask.getCreatedAt()
        );
    }

    // ==========================================================================
    // 更新子任务
    // ==========================================================================

    /**
     * 更新子任务
     * 
     * 执行流程：
     * 1. 查询子任务并校验权限
     * 2. 更新标题（如有）
     * 3. 更新重复设置（如有）
     * 4. 持久化
     * 
     * @param subTaskId 子任务 ID
     * @param userId 当前用户 ID
     * @param title 新标题（null 表示不修改）
     * @param repeatType 新重复类型（null 表示不修改）
     * @param repeatConfig 新重复配置
     * @return 更新结果
     */
    public UpdateSubTaskResult updateSubTask(String subTaskId, String userId,
                                              String title, String repeatType,
                                              Map<String, Object> repeatConfig, LocalDate date) {
        log.info("开始更新子任务: subTaskId={}, userId={}, date={}", subTaskId, userId, date);

        // ========== 1. 查询子任务 ==========
        Optional<SubTask> subTaskOpt = subTaskRepository.findBySubTaskId(subTaskId);
        if (!subTaskOpt.isPresent()) {
            log.warn("子任务不存在: subTaskId={}", subTaskId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        SubTask subTask = subTaskOpt.get();

        // 校验用户权限
        if (!subTask.getUserId().equals(userId)) {
            log.warn("用户无权访问此子任务: subTaskId={}, taskOwner={}, requestUser={}",
                    subTaskId, subTask.getUserId(), userId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // ========== 2. 重复任务按日隔离 ==========
        subTask = resolveSubTaskForDate(subTask, date, userId);

        // ========== 3. 更新字段 ==========
        boolean updated = false;

        // 更新标题
        if (title != null) {
            String normalizedTitle = title.trim();
            if (normalizedTitle.length() < 1 || normalizedTitle.length() > 50) {
                throw new BusinessException(ErrorCode.TASK_TITLE_INVALID);
            }
            subTask.updateTitle(normalizedTitle);
            updated = true;
            log.debug("更新子任务标题: {}", normalizedTitle);
        }

        // 更新重复设置
        if (repeatType != null) {
            RepeatType newRepeatType = parseRepeatType(repeatType);
            String newRepeatConfig = validateRepeatConfig(newRepeatType, repeatConfig);
            subTask.updateRepeat(newRepeatType, newRepeatConfig);
            updated = true;
            log.debug("更新子任务重复设置: type={}, config={}", newRepeatType, newRepeatConfig);
        }

        // ========== 3. 持久化 ==========
        if (updated) {
            subTaskRepository.updateSubTask(subTask);
            log.info("子任务更新成功: subTaskId={}", subTaskId);
        } else {
            log.info("子任务无更新: subTaskId={}", subTaskId);
        }

        return new UpdateSubTaskResult(
                subTask.getSubTaskId(),
                subTask.getTitle(),
                subTask.getUpdatedAt()
        );
    }

    // ==========================================================================
    // 删除子任务
    // ==========================================================================

    /**
     * 删除子任务（逻辑删除）
     * 
     * 执行流程：
     * 1. 查询子任务并校验权限
     * 2. 逻辑删除
     * 
     * @param subTaskId 子任务 ID
     * @param userId 当前用户 ID
     */
    public void deleteSubTask(String subTaskId, String userId, LocalDate date) {
        log.info("开始删除子任务: subTaskId={}, userId={}, date={}", subTaskId, userId, date);

        // ========== 1. 查询子任务 ==========
        Optional<SubTask> subTaskOpt = subTaskRepository.findBySubTaskId(subTaskId);
        if (!subTaskOpt.isPresent()) {
            // 幂等：子任务不存在，视为已删除
            log.debug("子任务不存在，视为已删除: subTaskId={}", subTaskId);
            return;
        }
        SubTask subTask = subTaskOpt.get();

        // 校验用户权限
        if (!subTask.getUserId().equals(userId)) {
            log.warn("用户无权访问此子任务: subTaskId={}, taskOwner={}, requestUser={}",
                    subTaskId, subTask.getUserId(), userId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // ========== 2. 重复任务按日隔离 ==========
        subTask = resolveSubTaskForDate(subTask, date, userId);

        // ========== 3. 逻辑删除 ==========
        subTaskRepository.softDelete(subTask.getSubTaskId());
        log.info("子任务删除成功: subTaskId={}, parentTaskId={}", subTask.getSubTaskId(), subTask.getParentTaskId());
    }

    // ==========================================================================
    // 完成/反完成子任务
    // ==========================================================================

    /**
     * 完成/反完成子任务
     * 
     * 执行流程：
     * 1. 查询子任务并校验权限
     * 2. 切换完成状态
     * 3. 持久化
     * 4. 查询父任务最新状态返回
     * 
     * @param subTaskId 子任务 ID
     * @param userId 当前用户 ID
     * @param completed true=完成，false=反完成
     * @return 切换结果（含父任务最新状态）
     */
    public ToggleSubTaskCompleteResult toggleSubTaskComplete(String subTaskId, String userId,
                                                              boolean completed, LocalDate date) {
        log.info("开始{}子任务: subTaskId={}, userId={}, date={}", completed ? "完成" : "反完成", subTaskId, userId, date);

        // ========== 1. 查询子任务 ==========
        Optional<SubTask> subTaskOpt = subTaskRepository.findBySubTaskId(subTaskId);
        if (!subTaskOpt.isPresent()) {
            log.warn("子任务不存在: subTaskId={}", subTaskId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        SubTask subTask = subTaskOpt.get();

        // 校验用户权限
        if (!subTask.getUserId().equals(userId)) {
            log.warn("用户无权访问此子任务: subTaskId={}, taskOwner={}, requestUser={}",
                    subTaskId, subTask.getUserId(), userId);
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // ========== 2. 重复任务按日隔离 ==========
        subTask = resolveSubTaskForDate(subTask, date, userId);

        // ========== 3. 切换完成状态 ==========
        if (completed) {
            subTask.complete();
        } else {
            subTask.uncomplete();
        }
        subTaskRepository.updateStatus(subTask);
        log.info("子任务状态更新成功: subTaskId={}, status={}", subTask.getSubTaskId(), subTask.getStatus());

        // ========== 4. 查询父任务最新状态 ==========
        String parentTaskId = subTask.getParentTaskId();
        String parentTaskStatus = "INCOMPLETE";

        Optional<Task> parentOpt = taskRepository.findByTaskId(parentTaskId);
        if (parentOpt.isPresent()) {
            parentTaskStatus = parentOpt.get().getStatus().name();
            log.debug("父任务当前状态: parentTaskId={}, status={}", parentTaskId, parentTaskStatus);
        }

        return new ToggleSubTaskCompleteResult(
                subTask.getSubTaskId(),
                subTask.getStatus().name(),
                subTask.getCompletedAt(),
                parentTaskId,
                parentTaskStatus
        );
    }

    // ==========================================================================
    // 查询任务列表
    // ==========================================================================

    /**
     * 查询指定日期的任务列表
     * 
     * 执行流程：
     * 1. 查询指定日期的实例任务
     * 2. 查询重复模板，动态生成虚拟任务
     * 3. 合并实例 + 虚拟任务
     * 4. 批量查询子任务
     * 5. 按优先级分组
     */
    public TaskListResult queryTasksByDate(String userId, LocalDate date, boolean showCompleted) {
        log.info("开始查询任务列表: userId={}, date={}, showCompleted={}", userId, date, showCompleted);

        // ========== 1. 查询指定日期的实例任务 ==========
        List<Task> rawInstanceTasks;
        if (showCompleted) {
            rawInstanceTasks = taskRepository.findByUserIdAndDate(userId, date);
        } else {
            rawInstanceTasks = taskRepository.findIncompleteByUserIdAndDate(userId, date);
        }
        log.debug("原始实例任务查询完成: count={}", rawInstanceTasks.size());

        // 去重：如果模板和它的实例同时出现在同一天（起始日被实例化的情况），只保留实例
        Set<String> instancedTemplateIds = new HashSet<String>();
        for (Task task : rawInstanceTasks) {
            if (task.isRepeatInstance() && task.getRepeatParentId() != null) {
                instancedTemplateIds.add(task.getRepeatParentId());
            }
        }
        List<Task> instanceTasks = new ArrayList<Task>();
        for (Task task : rawInstanceTasks) {
            // 如果是重复模板且该模板已有实例，跳过模板（保留实例）
            if (task.isRepeatTemplate() && instancedTemplateIds.contains(task.getTaskId())) {
                log.debug("去重：跳过已有实例的模板任务: templateId={}", task.getTaskId());
                continue;
            }
            instanceTasks.add(task);
        }
        log.debug("去重后实例任务: count={}", instanceTasks.size());

        // ========== 2. 查询重复模板，生成虚拟任务 ==========
        List<Task> repeatTemplates = taskRepository.findRepeatTemplates(userId, date);
        log.debug("重复模板查询完成: templateCount={}", repeatTemplates.size());

        // 查询该日期所有已存在实例的父任务ID（不受 showCompleted 影响，用于去重）
        List<String> existingParentIdList = taskRepository.findExistingInstanceParentIds(userId, date);
        Set<String> existingParentIds = new HashSet<String>(existingParentIdList);
        log.debug("已存在实例的父任务ID: count={}", existingParentIds.size());

        // 收集 instanceTasks 中的任务ID（用于过滤模板起始日当天的情况）
        Set<String> instanceTaskIds = new HashSet<String>();
        for (Task task : instanceTasks) {
            instanceTaskIds.add(task.getTaskId());
        }

        List<Task> virtualTasks = new ArrayList<Task>();
        for (Task template : repeatTemplates) {
            // 跳过已有实例的模板
            if (existingParentIds.contains(template.getTaskId())) {
                continue;
            }
            // 跳过模板自身已在实例列表中（起始日当天）
            if (instanceTaskIds.contains(template.getTaskId())) {
                continue;
            }
            // 匹配日期
            if (RepeatTaskMatcher.matches(template, date)) {
                Task virtualTask = VirtualTaskBuilder.buildVirtualTask(template, date);
                virtualTasks.add(virtualTask);
            }
        }

        log.debug("虚拟任务生成完成: virtualCount={}", virtualTasks.size());

        // ========== 3. 合并 ==========
        List<Task> allTasks = new ArrayList<Task>();
        allTasks.addAll(instanceTasks);
        allTasks.addAll(virtualTasks);
        log.debug("任务合并完成: totalCount={} (实例={}, 虚拟={})",
                allTasks.size(), instanceTasks.size(), virtualTasks.size());

        // ========== 4. 批量查询子任务 ==========
        List<String> taskIds = new ArrayList<String>();
        for (Task task : allTasks) {
            taskIds.add(task.getTaskId());
            // 虚拟任务额外添加模板ID（继承模板的子任务结构）
            if (task.isRepeatInstance() && task.getRepeatParentId() != null) {
                if (!taskIds.contains(task.getRepeatParentId())) {
                    taskIds.add(task.getRepeatParentId());
                }
            }
        }
        List<SubTask> allSubTasks = subTaskRepository.findByParentTaskIds(taskIds);
        log.debug("子任务查询完成: subTaskCount={}", allSubTasks.size());

        Map<String, List<SubTask>> subTaskMap = new HashMap<String, List<SubTask>>();
        for (SubTask subTask : allSubTasks) {
            String parentId = subTask.getParentTaskId();
            if (!subTaskMap.containsKey(parentId)) {
                subTaskMap.put(parentId, new ArrayList<SubTask>());
            }
            subTaskMap.get(parentId).add(subTask);
        }

        // ========== 5. 按优先级分组 ==========
        Map<String, List<TaskWithSubTasksDTO>> tasksByPriority = new LinkedHashMap<String, List<TaskWithSubTasksDTO>>();
        tasksByPriority.put("P0", new ArrayList<TaskWithSubTasksDTO>());
        tasksByPriority.put("P1", new ArrayList<TaskWithSubTasksDTO>());
        tasksByPriority.put("P2", new ArrayList<TaskWithSubTasksDTO>());
        tasksByPriority.put("P3", new ArrayList<TaskWithSubTasksDTO>());

        Map<String, Boolean> hasUncheckedTasks = new HashMap<String, Boolean>();
        hasUncheckedTasks.put("P0", false);
        hasUncheckedTasks.put("P1", false);
        hasUncheckedTasks.put("P2", false);
        hasUncheckedTasks.put("P3", false);

        for (Task task : allTasks) {
            String priorityStr = task.getPriority().name();

            if (task.getStatus() == TaskStatus.INCOMPLETE) {
                hasUncheckedTasks.put(priorityStr, true);
            }

            // 构建子任务 DTO
            // 优先使用实例自身的子任务，没有则继承模板的子任务
            List<SubTaskDTO> subTaskDTOs = new ArrayList<SubTaskDTO>();
            String subTaskLookupId = task.getTaskId();
            if (task.isRepeatInstance() && task.getRepeatParentId() != null) {
                // 实例任务：先检查实例自身是否有子任务
                boolean hasOwnSubTasks = subTaskMap.containsKey(task.getTaskId())
                        && !subTaskMap.get(task.getTaskId()).isEmpty();
                if (!hasOwnSubTasks) {
                    // 没有实例子任务，继承模板的子任务
                    subTaskLookupId = task.getRepeatParentId();
                }
            }
            List<SubTask> subTasks = subTaskMap.get(subTaskLookupId);
            if (subTasks != null) {
                for (SubTask subTask : subTasks) {
                    subTaskDTOs.add(new SubTaskDTO(
                            subTask.getSubTaskId(),
                            subTask.getParentTaskId(),
                            subTask.getTitle(),
                            subTask.getStatus().name(),
                            subTask.getRepeatType().name(),
                            subTask.getCompletedAt()
                    ));
                }
            }

            TaskWithSubTasksDTO taskDTO = new TaskWithSubTasksDTO(
                    task.getTaskId(),
                    task.getTitle(),
                    priorityStr,
                    task.getStatus().name(),
                    task.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    task.getCreatedAt(),
                    task.getCompletedAt(),
                    task.getRepeatType().name(),
                    task.getRepeatConfig(),
                    task.isRepeatInstance(),
                    task.getRepeatParentId(),
                    subTaskDTOs
            );

            tasksByPriority.get(priorityStr).add(taskDTO);
        }

        log.info("任务列表查询完成: date={}, P0={}, P1={}, P2={}, P3={}",
                date,
                tasksByPriority.get("P0").size(),
                tasksByPriority.get("P1").size(),
                tasksByPriority.get("P2").size(),
                tasksByPriority.get("P3").size());

        return new TaskListResult(
                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                hasUncheckedTasks,
                tasksByPriority
        );
    }

    // ==========================================================================
    // 辅助方法
    // ==========================================================================

    /**
     * 标准化任务标题
     */
    private String normalizeTitle(String title) {
        if (title == null) {
            throw new BusinessException(ErrorCode.TASK_TITLE_INVALID);
        }
        return title.trim();
    }

    /**
     * 校验任务日期范围（今天 ±365 天）
     */
    private void validateDateRange(LocalDate date) {
        if (date == null) {
            throw new BusinessException(ErrorCode.TASK_DATE_OUT_OF_RANGE);
        }
        LocalDate today = LocalDate.now();
        LocalDate min = today.minusDays(DATE_RANGE_DAYS);
        LocalDate max = today.plusDays(DATE_RANGE_DAYS);
        if (date.isBefore(min) || date.isAfter(max)) {
            throw new BusinessException(ErrorCode.TASK_DATE_OUT_OF_RANGE);
        }
    }

    /**
     * 解析任务优先级
     */
    private TaskPriority parsePriority(String priority) {
        if (priority == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        try {
            return TaskPriority.valueOf(priority);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * 解析重复类型
     */
    private RepeatType parseRepeatType(String repeatType) {
        if (repeatType == null) {
            return RepeatType.NONE;
        }
        try {
            return RepeatType.valueOf(repeatType);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
        }
    }

    /**
     * 校验重复配置并序列化
     */
    private String validateRepeatConfig(RepeatType repeatType, Map<String, Object> repeatConfig) {
        // NONE 类型：不需要重复配置
        if (repeatType == RepeatType.NONE) {
            return null;
        }

        // DAILY 类型：无需配置，允许 null 或空 map
        if (repeatType == RepeatType.DAILY) {
            // DAILY 不需要 repeatConfig，忽略传入的配置
            return null;
        }

        // WEEKLY 和 MONTHLY 需要配置
        if (repeatConfig == null || repeatConfig.isEmpty()) {
            throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
        }

        if (repeatType == RepeatType.WEEKLY) {
            Object weekdays = repeatConfig.get("weekdays");
            if (!(weekdays instanceof List)) {
                log.warn("WEEKLY 配置缺少 weekdays 字段或格式错误");
                throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
            }
            List<?> values = (List<?>) weekdays;
            if (values.isEmpty()) {
                log.warn("WEEKLY 配置 weekdays 为空");
                throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
            }
            // 标准化存储格式，确保 weekdays 是数字数组
            java.util.List<Integer> normalizedDays = new java.util.ArrayList<Integer>();
            for (Object value : values) {
                int day;
                if (value instanceof Number) {
                    day = ((Number) value).intValue();
                } else if (value instanceof String) {
                    try {
                        day = Integer.parseInt((String) value);
                    } catch (NumberFormatException e) {
                        log.warn("WEEKLY 配置 weekdays 元素格式错误: {}", value);
                        throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
                    }
                } else {
                    log.warn("WEEKLY 配置 weekdays 元素类型错误: {}", value.getClass().getName());
                    throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
                }
                if (day < 1 || day > 7) {
                    log.warn("WEEKLY 配置 weekdays 元素范围错误: {}", day);
                    throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
                }
                normalizedDays.add(day);
            }
            java.util.Map<String, Object> normalizedConfig = new java.util.HashMap<String, Object>();
            normalizedConfig.put("weekdays", normalizedDays);
            return JSON.toJSONString(normalizedConfig);
        }

        if (repeatType == RepeatType.MONTHLY) {
            Object dayOfMonthObj = repeatConfig.get("dayOfMonth");
            if (dayOfMonthObj == null) {
                log.warn("MONTHLY 配置缺少 dayOfMonth 字段");
                throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
            }
            int day;
            if (dayOfMonthObj instanceof Number) {
                day = ((Number) dayOfMonthObj).intValue();
            } else if (dayOfMonthObj instanceof String) {
                // 支持字符串形式的数字
                try {
                    day = Integer.parseInt((String) dayOfMonthObj);
                } catch (NumberFormatException e) {
                    log.warn("MONTHLY 配置 dayOfMonth 格式错误: {}", dayOfMonthObj);
                    throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
                }
            } else {
                log.warn("MONTHLY 配置 dayOfMonth 类型错误: {}", dayOfMonthObj.getClass().getName());
                throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
            }
            if (day < 1 || day > 31) {
                log.warn("MONTHLY 配置 dayOfMonth 范围错误: {}", day);
                throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
            }
            // 标准化存储格式，确保 dayOfMonth 是数字类型
            java.util.Map<String, Object> normalizedConfig = new java.util.HashMap<String, Object>();
            normalizedConfig.put("dayOfMonth", day);
            return JSON.toJSONString(normalizedConfig);
        }

        throw new BusinessException(ErrorCode.TASK_REPEAT_CONFIG_INVALID);
    }

    /**
     * 组装创建任务响应
     */
    private CreateTaskResult buildCreateResult(Task task) {
        return new CreateTaskResult(
                task.getTaskId(),
                task.getTitle(),
                task.getPriority().name(),
                task.getStatus().name(),
                task.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                task.getRepeatType().name(),
                task.getRepeatConfig(),
                task.getRepeatEndDate(),
                task.getCreatedAt()
        );
    }

    /**
     * 组装实例化结果
     */
    private MaterializeResult buildMaterializeResult(Task instance) {
        return new MaterializeResult(
                instance.getTaskId(),
                instance.getTitle(),
                instance.getPriority().name(),
                instance.getStatus().name(),
                instance.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                instance.getRepeatType().name(),
                instance.getRepeatConfig(),
                instance.isRepeatInstance(),
                instance.getRepeatParentId(),
                instance.getCreatedAt()
        );
    }

    /**
     * 解析虚拟任务 ID
     * 
     * 格式：{模板UUID}_{YYYY-MM-DD}
     * 示例：abc-def-123_2026-02-10
     */
    private VirtualIdInfo parseVirtualTaskId(String virtualTaskId) {
        if (virtualTaskId == null || virtualTaskId.isEmpty()) {
            throw new BusinessException(ErrorCode.TASK_VIRTUAL_ID_INVALID);
        }

        // 从末尾查找日期部分（最后 11 个字符：_YYYY-MM-DD）
        int lastUnderscore = virtualTaskId.lastIndexOf('_');
        if (lastUnderscore < 1) {
            throw new BusinessException(ErrorCode.TASK_VIRTUAL_ID_INVALID);
        }

        String templateId = virtualTaskId.substring(0, lastUnderscore);
        String dateStr = virtualTaskId.substring(lastUnderscore + 1);

        if (templateId.isEmpty()) {
            throw new BusinessException(ErrorCode.TASK_VIRTUAL_ID_INVALID);
        }

        LocalDate instanceDate;
        try {
            instanceDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.TASK_VIRTUAL_ID_INVALID);
        }

        return new VirtualIdInfo(templateId, instanceDate);
    }

    /**
     * 虚拟任务 ID 解析结果
     */
    private static class VirtualIdInfo {
        final String templateId;
        final LocalDate instanceDate;

        VirtualIdInfo(String templateId, LocalDate instanceDate) {
            this.templateId = templateId;
            this.instanceDate = instanceDate;
        }
    }
}
