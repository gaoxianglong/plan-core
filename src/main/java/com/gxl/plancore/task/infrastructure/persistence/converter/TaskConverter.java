package com.gxl.plancore.task.infrastructure.persistence.converter;

import com.gxl.plancore.task.domain.entity.Task;
import com.gxl.plancore.task.domain.valueobject.RepeatType;
import com.gxl.plancore.task.domain.valueobject.TaskPriority;
import com.gxl.plancore.task.domain.valueobject.TaskStatus;
import com.gxl.plancore.task.infrastructure.persistence.po.TaskPO;

/**
 * 任务转换器
 */
public class TaskConverter {

    /**
     * 将领域对象转换为持久化对象
     */
    public static TaskPO toPO(Task task) {
        TaskPO po = new TaskPO();
        po.setTaskId(task.getTaskId());
        po.setUserId(task.getUserId());
        po.setTitle(task.getTitle());
        po.setPriority(task.getPriority().name());
        po.setDate(task.getDate());
        po.setStatus(task.getStatus().name());
        po.setCompletedAt(task.getCompletedAt());
        po.setRepeatType(task.getRepeatType().name());
        po.setRepeatConfig(task.getRepeatConfig());
        po.setRepeatEndDate(task.getRepeatEndDate());
        po.setRepeatInstance(task.isRepeatInstance());
        po.setRepeatParentId(task.getRepeatParentId());
        po.setCreatedAt(task.getCreatedAt());
        po.setUpdatedAt(task.getUpdatedAt());
        po.setDeletedAt(task.getDeletedAt());
        return po;
    }

    /**
     * 将持久化对象转换为领域对象
     */
    public static Task toDomain(TaskPO po) {
        return Task.reconstitute(
                po.getTaskId(),
                po.getUserId(),
                po.getTitle(),
                TaskPriority.valueOf(po.getPriority()),
                po.getDate(),
                TaskStatus.valueOf(po.getStatus()),
                po.getCompletedAt(),
                RepeatType.valueOf(po.getRepeatType()),
                po.getRepeatConfig(),
                po.getRepeatEndDate(),
                po.isRepeatInstance(),
                po.getRepeatParentId(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                po.getDeletedAt()
        );
    }
}
