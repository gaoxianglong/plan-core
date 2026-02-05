package com.gxl.plancore.user.infrastructure.persistence.mapper;

import com.gxl.plancore.user.infrastructure.persistence.po.UserPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 MyBatis Mapper
 */
@Mapper
public interface UserMapper {
    
    @Select("SELECT id, user_id, email, password, nickname, avatar, ip_location, " +
            "consecutive_days, last_check_in_date, nickname_modify_count, nickname_first_modify_at, " +
            "created_at, updated_at, deleted_at " +
            "FROM user WHERE user_id = #{userId} AND deleted_at IS NULL")
    UserPO findByUserId(@Param("userId") String userId);
    
    @Select("SELECT id, user_id, email, password, nickname, avatar, ip_location, " +
            "consecutive_days, last_check_in_date, nickname_modify_count, nickname_first_modify_at, " +
            "created_at, updated_at, deleted_at " +
            "FROM user WHERE email = #{email} AND deleted_at IS NULL")
    UserPO findByEmail(@Param("email") String email);
    
    @Select("SELECT COUNT(1) > 0 FROM user WHERE email = #{email} AND deleted_at IS NULL")
    boolean existsByEmail(@Param("email") String email);
    
    @Insert("INSERT INTO user (user_id, email, password, nickname, avatar, ip_location, " +
            "consecutive_days, last_check_in_date, nickname_modify_count, nickname_first_modify_at, " +
            "created_at, updated_at) " +
            "VALUES (#{userId}, #{email}, #{password}, #{nickname}, #{avatar}, #{ipLocation}, " +
            "#{consecutiveDays}, #{lastCheckInDate}, #{nicknameModifyCount}, #{nicknameFirstModifyAt}, " +
            "#{createdAt}, #{updatedAt})")
    int insert(UserPO userPO);
    
    @org.apache.ibatis.annotations.Update("UPDATE user SET password = #{password}, updated_at = #{updatedAt} " +
            "WHERE user_id = #{userId} AND deleted_at IS NULL")
    int updatePassword(@Param("userId") String userId, @Param("password") String password, 
                       @Param("updatedAt") java.time.Instant updatedAt);
}
