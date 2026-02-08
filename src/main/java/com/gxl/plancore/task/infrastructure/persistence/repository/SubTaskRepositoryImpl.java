package com.gxl.plancore.task.infrastructure.persistence.repository;

import com.gxl.plancore.task.domain.entity.SubTask;
import com.gxl.plancore.task.domain.repository.SubTaskRepository;
import com.gxl.plancore.task.infrastructure.persistence.converter.SubTaskConverter;
import com.gxl.plancore.task.infrastructure.persistence.mapper.SubTaskMapper;
import com.gxl.plancore.task.infrastructure.persistence.po.SubTaskPO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 子任务仓储实现
 */
@Repository
public class SubTaskRepositoryImpl implements SubTaskRepository {

    private final SubTaskMapper subTaskMapper;

    /**
     * 构造子任务仓储实现
     */
    public SubTaskRepositoryImpl(SubTaskMapper subTaskMapper) {
        this.subTaskMapper = subTaskMapper;
    }

    /**
     * 根据父任务ID列表查询子任务
     */
    @Override
    public List<SubTask> findByParentTaskIds(List<String> parentTaskIds) {
        if (parentTaskIds == null || parentTaskIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<SubTaskPO> poList = subTaskMapper.findByParentTaskIds(parentTaskIds);
        List<SubTask> result = new ArrayList<SubTask>();
        for (SubTaskPO po : poList) {
            result.add(SubTaskConverter.toDomain(po));
        }
        return result;
    }

    /**
     * 根据父任务ID查询子任务
     */
    @Override
    public List<SubTask> findByParentTaskId(String parentTaskId) {
        List<SubTaskPO> poList = subTaskMapper.findByParentTaskId(parentTaskId);
        List<SubTask> result = new ArrayList<SubTask>();
        for (SubTaskPO po : poList) {
            result.add(SubTaskConverter.toDomain(po));
        }
        return result;
    }

    /**
     * 统计父任务下未完成的子任务数量
     */
    @Override
    public int countIncompleteByParentTaskId(String parentTaskId) {
        return subTaskMapper.countIncompleteByParentTaskId(parentTaskId);
    }

    /**
     * 统计父任务下所有子任务数量（不含已删除）
     */
    @Override
    public int countByParentTaskId(String parentTaskId) {
        return subTaskMapper.countByParentTaskId(parentTaskId);
    }

    /**
     * 保存子任务
     */
    @Override
    public void save(SubTask subTask) {
        SubTaskPO po = SubTaskConverter.toPO(subTask);
        subTaskMapper.insert(po);
    }

    /**
     * 根据子任务ID查询子任务
     */
    @Override
    public Optional<SubTask> findBySubTaskId(String subTaskId) {
        SubTaskPO po = subTaskMapper.findBySubTaskId(subTaskId);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(SubTaskConverter.toDomain(po));
    }

    /**
     * 更新子任务
     */
    @Override
    public void updateSubTask(SubTask subTask) {
        SubTaskPO po = SubTaskConverter.toPO(subTask);
        subTaskMapper.updateSubTask(po);
    }

    /**
     * 逻辑删除子任务
     */
    @Override
    public void softDelete(String subTaskId) {
        subTaskMapper.softDelete(subTaskId);
    }

    /**
     * 更新子任务状态
     */
    @Override
    public void updateStatus(SubTask subTask) {
        SubTaskPO po = SubTaskConverter.toPO(subTask);
        subTaskMapper.updateStatus(po);
    }
}
