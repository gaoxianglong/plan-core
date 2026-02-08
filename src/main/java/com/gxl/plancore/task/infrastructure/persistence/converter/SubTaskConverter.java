package com.gxl.plancore.task.infrastructure.persistence.converter;

import com.gxl.plancore.task.domain.entity.SubTask;
import com.gxl.plancore.task.domain.valueobject.RepeatType;
import com.gxl.plancore.task.domain.valueobject.TaskStatus;
import com.gxl.plancore.task.infrastructure.persistence.po.SubTaskPO;

/**
 * 子任务转换器
 */
public class SubTaskConverter {

    /**
     * 将持久化对象转换为领域对象
     */
    public static SubTask toDomain(SubTaskPO po) {
        return SubTask.reconstitute(
                po.getSubTaskId(),
                po.getParentTaskId(),
                po.getUserId(),
                po.getTitle(),
                TaskStatus.valueOf(po.getStatus()),
                po.getCompletedAt(),
                RepeatType.valueOf(po.getRepeatType()),
                po.getRepeatConfig(),
                po.isRepeatInstance(),
                po.getRepeatParentId(),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }

    /**
     * 将领域对象转换为持久化对象
     */
    public static SubTaskPO toPO(SubTask subTask) {
        SubTaskPO po = new SubTaskPO();
        po.setSubTaskId(subTask.getSubTaskId());
        po.setParentTaskId(subTask.getParentTaskId());
        po.setUserId(subTask.getUserId());
        po.setTitle(subTask.getTitle());
        po.setStatus(subTask.getStatus().name());
        po.setCompletedAt(subTask.getCompletedAt());
        po.setRepeatType(subTask.getRepeatType().name());
        po.setRepeatConfig(subTask.getRepeatConfig());
        po.setRepeatInstance(subTask.isRepeatInstance());
        po.setRepeatParentId(subTask.getRepeatParentId());
        po.setCreatedAt(subTask.getCreatedAt());
        po.setUpdatedAt(subTask.getUpdatedAt());
        return po;
    }
}
