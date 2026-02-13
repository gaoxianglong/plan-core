package com.gxl.plancore.checkin.infrastructure.persistence.converter;

import com.gxl.plancore.checkin.domain.entity.CheckIn;
import com.gxl.plancore.checkin.infrastructure.persistence.po.CheckInPO;

/**
 * 打卡 PO/领域对象转换器
 */
public class CheckInConverter {

    private CheckInConverter() {
    }

    /**
     * PO 转 领域对象
     */
    public static CheckIn toDomain(CheckInPO po) {
        if (po == null) {
            return null;
        }
        return CheckIn.reconstitute(
                po.getCheckInId(),
                po.getUserId(),
                po.getDate(),
                po.getCheckedAt(),
                po.getConsecutiveDays(),
                po.getCreatedAt()
        );
    }

    /**
     * 领域对象 转 PO
     */
    public static CheckInPO toPO(CheckIn checkIn) {
        if (checkIn == null) {
            return null;
        }
        CheckInPO po = new CheckInPO();
        po.setCheckInId(checkIn.getCheckInId());
        po.setUserId(checkIn.getUserId());
        po.setDate(checkIn.getDate());
        po.setCheckedAt(checkIn.getCheckedAt());
        po.setConsecutiveDays(checkIn.getConsecutiveDays());
        po.setCreatedAt(checkIn.getCreatedAt());
        return po;
    }
}
