-- =====================================================
-- 做计划 APP 数据库表设计
-- =====================================================
-- 版本：v1.0
-- 日期：2026-01-27
-- 依据：PRD v2.0、技术约束&规范.md
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci
-- =====================================================

-- =====================================================
-- 设计说明
-- =====================================================
-- 1. 不使用外键约束，原因：
--    - 避免跨表锁，提升并发性能
--    - 便于后期数据迁移和分库演进
--    - 在应用层保证引用完整性
-- 2. 所有表包含公共字段：
--    - created_at: 创建时间
--    - updated_at: 更新时间
--    - deleted_at: 删除时间（逻辑删除）
-- 3. 时间字段统一使用 DATETIME(3) 存储 UTC 时间
-- 4. 主键统一使用 BIGINT UNSIGNED AUTO_INCREMENT
-- 5. UUID 字段使用 CHAR(36) 存储
-- 6. 枚举值使用 VARCHAR 存储（便于扩展）
-- 7. 索引策略：
--    - 高频查询字段建立索引
--    - 联合索引遵循最左前缀原则
--    - 唯一约束保证数据完整性
-- =====================================================

-- =====================================================
-- 1. 用户表（user）
-- =====================================================
-- 说明：存储用户基本信息、账号状态、打卡统计
-- =====================================================

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID（业务主键）',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱（登录唯一标识）',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称，1-20字符',
  `avatar` VARCHAR(100) NOT NULL DEFAULT 'avatar-1' COMMENT '头像标识（系统预设头像ID）',
  `ip_location` VARCHAR(50) DEFAULT NULL COMMENT 'IP属地（省 市）',
  
  -- 打卡统计
  `consecutive_days` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '连续打卡天数',
  `last_check_in_date` DATE DEFAULT NULL COMMENT '最近打卡日期（本地时区）',
  
  -- 昵称修改限制
  `nickname_modify_count` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '7天内昵称修改次数',
  `nickname_first_modify_at` DATETIME(3) DEFAULT NULL COMMENT '7天窗口首次修改时间',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '删除时间（UTC，逻辑删除）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_last_check_in` (`last_check_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 任务表（task）
-- =====================================================
-- 说明：存储任务信息，支持重复任务（虚拟展开 + 按需实例化）
--   - 普通任务：repeat_type=NONE, is_repeat_instance=0
--   - 重复模板：repeat_type=DAILY/WEEKLY/MONTHLY, is_repeat_instance=0
--     只存 1 条记录，查询时动态计算匹配日期生成虚拟任务
--   - 重复实例：is_repeat_instance=1, repeat_parent_id 指向模板
--     用户操作某天的重复任务后按需创建
-- =====================================================

DROP TABLE IF EXISTS `task`;

CREATE TABLE `task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` CHAR(36) NOT NULL COMMENT '任务UUID（业务主键）',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID',
  
  -- 任务基本信息
  `title` VARCHAR(200) NOT NULL COMMENT '任务标题，1-100字符（UTF-8，支持emoji）',
  `priority` VARCHAR(10) NOT NULL COMMENT '优先级：P0/P1/P2/P3',
  `date` DATE NOT NULL COMMENT '归属日期（本地时区）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'INCOMPLETE' COMMENT '状态：INCOMPLETE（未完成）/COMPLETED（已完成）',
  
  -- 完成时间
  `completed_at` DATETIME(3) DEFAULT NULL COMMENT '完成时间（UTC）',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '删除时间（UTC，逻辑删除）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_user_date` (`user_id`, `date`, `deleted_at`),
  KEY `idx_user_status` (`user_id`, `status`, `deleted_at`),
  KEY `idx_date_priority` (`date`, `priority`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';


-- =====================================================
-- 3. 打卡记录表（check_in）
-- =====================================================
-- 说明：存储每日打卡记录
-- =====================================================

DROP TABLE IF EXISTS `check_in`;

CREATE TABLE `check_in` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `check_in_id` CHAR(36) NOT NULL COMMENT '打卡UUID（业务主键）',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID',
  
  -- 打卡信息
  `date` DATE NOT NULL COMMENT '打卡日期（本地时区）',
  `checked_at` DATETIME(3) NOT NULL COMMENT '打卡时间（UTC）',
  `consecutive_days` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '打卡时的连续天数（快照）',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_in_id` (`check_in_id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `date`),
  KEY `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡记录表';

-- =====================================================
-- 4. 专注会话表（focus_session）
-- =====================================================
-- 说明：存储专注倒计时会话记录
-- =====================================================

DROP TABLE IF EXISTS `focus_session`;

CREATE TABLE `focus_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` CHAR(36) NOT NULL COMMENT '会话UUID（业务主键）',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID',
  
  -- 会话信息
  `duration_seconds` INT UNSIGNED NOT NULL COMMENT '预设时长（秒），范围 600~3600（即 10~60 分钟）',
  `type` VARCHAR(20) NOT NULL COMMENT '专注类型：WORK/STUDY/READING/CODING/EXERCISE/MEDITATION/OTHER',
  `status` VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态：RUNNING（进行中）/INTERRUPTED（已中断）/COMPLETED（已完成）',
  
  -- 时间记录
  `start_at` DATETIME(3) NOT NULL COMMENT '开始时间（UTC）',
  `end_at` DATETIME(3) DEFAULT NULL COMMENT '结束时间（UTC）',
  `elapsed_seconds` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已完成秒数',
  
  -- 计入规则
  `end_type` VARCHAR(20) DEFAULT NULL COMMENT '结束类型：NATURAL（自然结束）/MANUAL（手动结束）',
  `counted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否计入总时长：0=否，1=是',
  `counted_seconds` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '计入的秒数',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_user_counted` (`user_id`, `counted`, `counted_seconds`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专注会话表';

-- =====================================================
-- 5. 用户专注统计表（user_focus_stats）
-- =====================================================
-- 说明：存储用户累计专注时长（汇总表，避免每次聚合计算）
-- =====================================================

DROP TABLE IF EXISTS `user_focus_stats`;

CREATE TABLE `user_focus_stats` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID',
  
  -- 统计数据
  `total_seconds` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计总专注时长（秒）',
  `total_hours` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计总专注时长（小时，向下取整）',
  `session_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '累计会话次数',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户专注统计表';

-- =====================================================
-- 6. 设备登录会话表（device_session）
-- =====================================================
-- 说明：存储多设备登录信息
-- =====================================================

DROP TABLE IF EXISTS `device_session`;

CREATE TABLE `device_session` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` CHAR(36) NOT NULL COMMENT '会话UUID（业务主键）',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID',
  
  -- 设备信息
  `device_id` CHAR(36) NOT NULL COMMENT '设备UUID',
  `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `platform` VARCHAR(20) NOT NULL COMMENT '平台：iOS/Android',
  `os_version` VARCHAR(50) DEFAULT NULL COMMENT '系统版本',
  `app_version` VARCHAR(50) NOT NULL COMMENT 'APP版本号',
  
  -- 登录信息
  `access_token` VARCHAR(500) NOT NULL COMMENT 'JWT访问token',
  `refresh_token` VARCHAR(500) NOT NULL COMMENT 'JWT刷新token',
  `expires_at` DATETIME(3) NOT NULL COMMENT 'access_token过期时间（UTC）',
  `refresh_expires_at` DATETIME(3) NOT NULL COMMENT 'refresh_token过期时间（UTC）',
  
  -- 登录记录
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最近登录IP',
  `last_login_at` DATETIME(3) NOT NULL COMMENT '最近登录时间（UTC）',
  `last_active_at` DATETIME(3) NOT NULL COMMENT '最近活跃时间（UTC）',
  
  -- 状态
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE（活跃）/LOGGED_OUT（已退出）',
  `logged_out_at` DATETIME(3) DEFAULT NULL COMMENT '退出时间（UTC）',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  UNIQUE KEY `uk_access_token` (`access_token`(255)),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_device` (`device_id`, `status`),
  KEY `idx_refresh_token` (`refresh_token`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备登录会话表';