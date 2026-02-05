package com.gxl.plancore.user.infrastructure.persistence.converter;

import com.gxl.plancore.user.domain.entity.User;
import com.gxl.plancore.user.domain.valueobject.Email;
import com.gxl.plancore.user.domain.valueobject.Nickname;
import com.gxl.plancore.user.domain.valueobject.Password;
import com.gxl.plancore.user.domain.valueobject.UserId;
import com.gxl.plancore.user.infrastructure.persistence.po.UserPO;

/**
 * User 领域对象与持久化对象转换器
 */
public class UserConverter {
    
    /**
     * PO -> 领域对象
     */
    public static User toDomain(UserPO po) {
        if (po == null) {
            return null;
        }
        return User.reconstruct(
                UserId.of(po.getUserId()),
                Email.of(po.getEmail()),
                Password.fromHashed(po.getPassword()),
                Nickname.of(po.getNickname()),
                po.getAvatar(),
                po.getIpLocation(),
                po.getConsecutiveDays() != null ? po.getConsecutiveDays() : 0,
                po.getLastCheckInDate(),
                po.getNicknameModifyCount() != null ? po.getNicknameModifyCount() : 0,
                po.getNicknameFirstModifyAt(),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
    
    /**
     * 领域对象 -> PO
     */
    public static UserPO toPO(User user) {
        if (user == null) {
            return null;
        }
        UserPO po = new UserPO();
        po.setUserId(user.getUserId().getValue());
        po.setEmail(user.getEmail().getValue());
        po.setPassword(user.getPassword().getHashedValue());
        po.setNickname(user.getNickname().getValue());
        po.setAvatar(user.getAvatar());
        po.setIpLocation(user.getIpLocation());
        po.setConsecutiveDays(user.getConsecutiveDays());
        po.setLastCheckInDate(user.getLastCheckInDate());
        po.setNicknameModifyCount(user.getNicknameModifyCount());
        po.setNicknameFirstModifyAt(user.getNicknameFirstModifyAt());
        po.setCreatedAt(user.getCreatedAt());
        po.setUpdatedAt(user.getUpdatedAt());
        return po;
    }
}
