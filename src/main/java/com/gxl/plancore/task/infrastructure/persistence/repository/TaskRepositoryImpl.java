package com.gxl.plancore.task.infrastructure.persistence.repository;

import com.gxl.plancore.task.domain.entity.Task;
import com.gxl.plancore.task.domain.repository.TaskRepository;
import com.gxl.plancore.task.infrastructure.persistence.converter.TaskConverter;
import com.gxl.plancore.task.infrastructure.persistence.mapper.TaskMapper;
import com.gxl.plancore.task.infrastructure.persistence.po.TaskPO;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 任务仓储实现
 */
@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final TaskMapper taskMapper;

    /**
     * 构造任务仓储实现
     */
    public TaskRepositoryImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    /**
     * 保存任务
     */
    @Override
    public void save(Task task) {
        TaskPO po = TaskConverter.toPO(task);
        taskMapper.insert(po);
    }

    /**
     * 根据任务ID查询任务
     */
    @Override
    public Optional<Task> findByTaskId(String taskId) {
        TaskPO po = taskMapper.findByTaskId(taskId);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(TaskConverter.toDomain(po));
    }

    /**
     * 统计用户指定日期的任务数量
     */
    @Override
    public int countByUserIdAndDate(String userId, LocalDate date) {
        return taskMapper.countByUserIdAndDate(userId, date);
    }

    /**
     * 查询用户指定日期的所有任务
     */
    @Override
    public List<Task> findByUserIdAndDate(String userId, LocalDate date) {
        List<TaskPO> poList = taskMapper.findByUserIdAndDate(userId, date);
        List<Task> result = new ArrayList<Task>();
        for (TaskPO po : poList) {
            result.add(TaskConverter.toDomain(po));
        }
        return result;
    }

    /**
     * 查询用户指定日期的未完成任务
     */
    @Override
    public List<Task> findIncompleteByUserIdAndDate(String userId, LocalDate date) {
        List<TaskPO> poList = taskMapper.findIncompleteByUserIdAndDate(userId, date);
        List<Task> result = new ArrayList<Task>();
        for (TaskPO po : poList) {
            result.add(TaskConverter.toDomain(po));
        }
        return result;
    }

    /**
     * 查询用户的所有重复模板任务
     */
    @Override
    public List<Task> findRepeatTemplates(String userId, LocalDate queryDate) {
        List<TaskPO> poList = taskMapper.findRepeatTemplates(userId, queryDate);
        List<Task> result = new ArrayList<Task>();
        for (TaskPO po : poList) {
            result.add(TaskConverter.toDomain(po));
        }
        return result;
    }

    /**
     * 查询指定日期已存在的重复实例
     */
    @Override
    public List<Task> findRepeatInstancesByDate(String userId, LocalDate date, List<String> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<TaskPO> poList = taskMapper.findRepeatInstancesByDate(userId, date, parentIds);
        List<Task> result = new ArrayList<Task>();
        for (TaskPO po : poList) {
            result.add(TaskConverter.toDomain(po));
        }
        return result;
    }

    /**
     * 更新任务
     */
    @Override
    public void update(Task task) {
        TaskPO po = TaskConverter.toPO(task);
        taskMapper.updateStatus(po);
    }

    /**
     * 查询指定日期所有重复实例的父任务ID
     */
    @Override
    public List<String> findExistingInstanceParentIds(String userId, LocalDate date) {
        return taskMapper.findExistingInstanceParentIds(userId, date);
    }

    /**
     * 更新任务信息
     */
    @Override
    public void updateTask(Task task) {
        TaskPO po = TaskConverter.toPO(task);
        taskMapper.updateTask(po);
    }

    /**
     * 逻辑删除任务
     */
    @Override
    public void softDelete(Task task) {
        TaskPO po = TaskConverter.toPO(task);
        taskMapper.softDelete(po);
    }

    /**
     * 逻辑删除重复模板的所有关联实例
     */
    @Override
    public int softDeleteByParentId(String templateId) {
        Instant now = Instant.now();
        return taskMapper.softDeleteByParentId(templateId, now, now);
    }

    /**
     * 保存已删除的任务实例
     */
    @Override
    public void saveDeletedInstance(Task task) {
        TaskPO po = TaskConverter.toPO(task);
        taskMapper.insertWithDeletedAt(po);
    }

    /**
     * 更新模板的重复结束日期
     */
    @Override
    public void updateRepeatEndDate(Task task) {
        TaskPO po = TaskConverter.toPO(task);
        taskMapper.updateRepeatEndDate(po);
    }

    /**
     * 逻辑删除指定模板在某日期及之后的所有实例
     */
    @Override
    public int softDeleteInstancesFromDate(String templateId, LocalDate fromDate) {
        Instant now = Instant.now();
        return taskMapper.softDeleteInstancesFromDate(templateId, fromDate, now, now);
    }
}
