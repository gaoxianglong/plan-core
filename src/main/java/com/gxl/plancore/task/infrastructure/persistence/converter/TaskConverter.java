package com.gxl.plancore.task.infrastructure.persistence.converter;

import com.gxl.plancore.task.domain.entity.Task;
import com.gxl.plancore.task.domain.valueobject.TaskPriority;
import com.gxl.plancore.task.domain.valueobject.TaskStatus;
import com.gxl.plancore.task.infrastructure.persistence.po.TaskPO;

/**
 * 任务 PO/领域对象转换器
 */
public class TaskConverter {

    private TaskConverter() {
    }

    /**
     * PO 转 领域对象
     */
    public static Task toDomain(TaskPO po) {
        if (po == null) {
            return null;
        }

        return Task.reconstitute(
                po.getTaskId(),
                po.getUserId(),
                po.getTitle(),
                TaskPriority.valueOf(po.getPriority()),
                po.getDate(),
                TaskStatus.valueOf(po.getStatus()),
                po.getCompletedAt(),
                po.getCreatedAt(),
                po.getUpdatedAt(),
                po.getDeletedAt()
        );
    }

    /**
     * 领域对象 转 PO
     */
    public static TaskPO toPO(Task task) {
        if (task == null) {
            return null;
        }

        TaskPO po = new TaskPO();
        po.setTaskId(task.getTaskId());
        po.setUserId(task.getUserId());
        po.setTitle(task.getTitle());
        po.setPriority(task.getPriority().name());
        po.setDate(task.getDate());
        po.setStatus(task.getStatus().name());
        po.setCompletedAt(task.getCompletedAt());
        po.setCreatedAt(task.getCreatedAt());
        po.setUpdatedAt(task.getUpdatedAt());
        po.setDeletedAt(task.getDeletedAt());
        return po;
    }
}
