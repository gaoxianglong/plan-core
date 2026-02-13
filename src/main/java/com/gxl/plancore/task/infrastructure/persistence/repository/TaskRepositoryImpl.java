package com.gxl.plancore.task.infrastructure.persistence.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gxl.plancore.task.domain.entity.Task;
import com.gxl.plancore.task.domain.repository.TaskRepository;
import com.gxl.plancore.task.domain.valueobject.TaskDateCount;
import com.gxl.plancore.task.infrastructure.persistence.converter.TaskConverter;
import com.gxl.plancore.task.infrastructure.persistence.mapper.TaskMapper;
import com.gxl.plancore.task.infrastructure.persistence.po.TaskCountPO;
import com.gxl.plancore.task.infrastructure.persistence.po.TaskPO;
import org.springframework.stereotype.Repository;

/**
 * 任务仓储实现
 */
@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final TaskMapper taskMapper;

    public TaskRepositoryImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public void save(Task task) {
        TaskPO po = TaskConverter.toPO(task);
        taskMapper.insert(po);
    }

    @Override
    public Optional<Task> findByTaskId(String taskId) {
        TaskPO po = taskMapper.findByTaskId(taskId);
        return Optional.ofNullable(TaskConverter.toDomain(po));
    }

    @Override
    public void update(Task task) {
        TaskPO po = TaskConverter.toPO(task);
        taskMapper.update(po);
    }

    @Override
    public int softDelete(String taskId) {
        return taskMapper.softDelete(taskId);
    }

    @Override
    public int countByUserIdAndDate(String userId, String date) {
        return taskMapper.countByUserIdAndDate(userId, date);
    }

    @Override
    public List<Task> findByUserIdAndDate(String userId, String date) {
        List<TaskPO> poList = taskMapper.findByUserIdAndDate(userId, date);
        List<Task> tasks = new ArrayList<>();
        for (TaskPO po : poList) {
            tasks.add(TaskConverter.toDomain(po));
        }
        return tasks;
    }

    @Override
    public List<Task> findByUserIdAndDateRange(String userId, String startDate, String endDate) {
        List<TaskPO> poList = taskMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        List<Task> tasks = new ArrayList<>();
        for (TaskPO po : poList) {
            tasks.add(TaskConverter.toDomain(po));
        }
        return tasks;
    }

    @Override
    public List<TaskDateCount> countGroupedByDateRange(String userId, String startDate, String endDate) {
        List<TaskCountPO> poList = taskMapper.countGroupedByDateRange(userId, startDate, endDate);
        List<TaskDateCount> result = new ArrayList<>();
        for (TaskCountPO po : poList) {
            result.add(new TaskDateCount(
                    po.getDate(),
                    po.getPriority(),
                    po.getStatus(),
                    po.getTaskCount()
            ));
        }
        return result;
    }
}
