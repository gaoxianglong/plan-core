package com.gxl.plancore.user.infrastructure.persistence.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.gxl.plancore.user.infrastructure.persistence.po.DeviceSessionPO;

/**
 * 设备会话 MyBatis Mapper
 */
@Mapper
public interface DeviceSessionMapper {

        @Insert("INSERT INTO device_session (session_id, user_id, device_id, device_name, platform, " +
                        "os_version, app_version, access_token, refresh_token, expires_at, refresh_expires_at, " +
                        "last_login_ip, last_login_at, last_active_at, status) " +
                        "VALUES (#{sessionId}, #{userId}, #{deviceId}, #{deviceName}, #{platform}, " +
                        "#{osVersion}, #{appVersion}, #{accessToken}, #{refreshToken}, #{expiresAt}, #{refreshExpiresAt}, "
                        +
                        "#{lastLoginIp}, #{lastLoginAt}, #{lastActiveAt}, #{status})")
        void insert(DeviceSessionPO po);

        @Select("SELECT id, session_id, user_id, device_id, device_name, platform, os_version, app_version, " +
                        "access_token, refresh_token, expires_at, refresh_expires_at, " +
                        "last_login_ip, last_login_at, last_active_at, status, logged_out_at, created_at, updated_at " +
                        "FROM device_session WHERE refresh_token = #{refreshToken}")
        DeviceSessionPO findByRefreshToken(@Param("refreshToken") String refreshToken);

        @Update("UPDATE device_session SET access_token = #{accessToken}, refresh_token = #{refreshToken}, " +
                        "expires_at = #{expiresAt}, refresh_expires_at = #{refreshExpiresAt}, " +
                        "last_active_at = #{lastActiveAt}, status = #{status}, logged_out_at = #{loggedOutAt}, " +
                        "updated_at = #{updatedAt} " +
                        "WHERE session_id = #{sessionId}")
        void update(DeviceSessionPO po);

        @Select("SELECT * FROM device_session WHERE session_id = #{sessionId}")
        DeviceSessionPO selectBySessionId(@Param("sessionId") String sessionId);

        @Select("SELECT * FROM device_session WHERE access_token = #{accessToken} AND status = 'ACTIVE'")
        DeviceSessionPO selectByAccessToken(@Param("accessToken") String accessToken);

        @Select("SELECT * FROM device_session WHERE user_id = #{userId} AND device_id = #{deviceId} AND status = 'ACTIVE'")
        DeviceSessionPO selectActiveByUserIdAndDeviceId(@Param("userId") String userId,
                        @Param("deviceId") String deviceId);

        @Select("SELECT * FROM device_session WHERE user_id = #{userId} AND status = 'ACTIVE' ORDER BY last_active_at DESC")
        List<DeviceSessionPO> selectActiveByUserId(@Param("userId") String userId);

        @Select("SELECT COUNT(*) FROM device_session WHERE user_id = #{userId} AND status = 'ACTIVE'")
        int countActiveByUserId(@Param("userId") String userId);

        @Update("UPDATE device_session SET status = 'LOGGED_OUT', logged_out_at = #{loggedOutAt}, updated_at = #{updatedAt} "
                        +
                        "WHERE user_id = #{userId} AND device_id = #{deviceId} AND status = 'ACTIVE'")
        int logoutByUserIdAndDeviceId(@Param("userId") String userId,
                        @Param("deviceId") String deviceId,
                        @Param("loggedOutAt") LocalDateTime loggedOutAt,
                        @Param("updatedAt") LocalDateTime updatedAt);

        @Update("UPDATE device_session SET status = 'LOGGED_OUT', logged_out_at = #{loggedOutAt}, updated_at = #{updatedAt} "
                        +
                        "WHERE user_id = #{userId} AND status = 'ACTIVE'")
        int logoutAllByUserId(@Param("userId") String userId,
                        @Param("loggedOutAt") LocalDateTime loggedOutAt,
                        @Param("updatedAt") LocalDateTime updatedAt);

        @Select("SELECT * FROM device_session WHERE user_id = #{userId} AND status = 'ACTIVE' " +
                        "ORDER BY last_active_at ASC LIMIT 1")
        DeviceSessionPO selectOldestActiveSession(@Param("userId") String userId);

        @Update("UPDATE device_session SET status = 'LOGGED_OUT', logged_out_at = #{loggedOutAt}, updated_at = #{updatedAt} "
                        +
                        "WHERE session_id = #{sessionId}")
        int logoutBySessionId(@Param("sessionId") String sessionId,
                        @Param("loggedOutAt") LocalDateTime loggedOutAt,
                        @Param("updatedAt") LocalDateTime updatedAt);
}
