package com.gxl.plancore.user.domain.repository;

import com.gxl.plancore.user.domain.entity.User;
import com.gxl.plancore.user.domain.valueobject.Email;
import com.gxl.plancore.user.domain.valueobject.UserId;

import java.util.Optional;

/**
 * 用户仓储接口（领域层定义）
 */
public interface UserRepository {
    
    /**
     * 根据用户ID查询
     */
    Optional<User> findByUserId(UserId userId);
    
    /**
     * 根据邮箱查询
     */
    Optional<User> findByEmail(Email email);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(Email email);
    
    /**
     * 保存用户
     */
    void save(User user);
    
    /**
     * 更新用户密码
     */
    void updatePassword(User user);
}
