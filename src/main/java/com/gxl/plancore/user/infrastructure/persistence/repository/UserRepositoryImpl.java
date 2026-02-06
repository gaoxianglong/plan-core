package com.gxl.plancore.user.infrastructure.persistence.repository;

import com.gxl.plancore.user.domain.entity.User;
import com.gxl.plancore.user.domain.repository.UserRepository;
import com.gxl.plancore.user.domain.valueobject.Email;
import com.gxl.plancore.user.domain.valueobject.UserId;
import com.gxl.plancore.user.infrastructure.persistence.converter.UserConverter;
import com.gxl.plancore.user.infrastructure.persistence.mapper.UserMapper;
import com.gxl.plancore.user.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现
 */
@Repository
public class UserRepositoryImpl implements UserRepository {
    
    private final UserMapper userMapper;
    
    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    @Override
    public Optional<User> findByUserId(UserId userId) {
        UserPO po = userMapper.findByUserId(userId.getValue());
        return Optional.ofNullable(UserConverter.toDomain(po));
    }
    
    @Override
    public Optional<User> findByEmail(Email email) {
        UserPO po = userMapper.findByEmail(email.getValue());
        return Optional.ofNullable(UserConverter.toDomain(po));
    }
    
    @Override
    public boolean existsByEmail(Email email) {
        return userMapper.existsByEmail(email.getValue());
    }
    
    @Override
    public void save(User user) {
        UserPO po = UserConverter.toPO(user);
        // 判断是新增还是更新
        UserPO existing = userMapper.findByUserId(user.getUserId().getValue());
        if (existing == null) {
            userMapper.insert(po);
        } else {
            // TODO: 实现完整 update 方法
            throw new UnsupportedOperationException("更新用户功能待实现");
        }
    }
    
    @Override
    public void updatePassword(User user) {
        userMapper.updatePassword(
                user.getUserId().getValue(),
                user.getPassword().getHashedValue(),
                user.getUpdatedAt()
        );
    }
    
    @Override
    public void updateProfile(User user) {
        userMapper.updateProfile(
                user.getUserId().getValue(),
                user.getNickname().getValue(),
                user.getAvatar(),
                user.getNicknameModifyCount(),
                user.getNicknameFirstModifyAt(),
                user.getUpdatedAt()
        );
    }
}
