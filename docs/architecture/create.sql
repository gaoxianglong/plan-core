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
  
  -- 重复任务配置
  `repeat_type` VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '重复类型：NONE（不重复）/DAILY（每日）/WEEKLY（每周）/MONTHLY（每月）',
  `repeat_config` JSON DEFAULT NULL COMMENT '重复配置（JSON）：WEEKLY={"weekdays":[1,3,5]}，MONTHLY={"dayOfMonth":1}',
  `repeat_end_date` DATE DEFAULT NULL COMMENT '重复结束日期（NULL表示永久重复，仅模板任务有值）',
  `is_repeat_instance` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为重复任务实例：0=模板/普通任务，1=实例（按需创建）',
  `repeat_parent_id` CHAR(36) DEFAULT NULL COMMENT '重复模板任务ID（仅实例有值，指向模板的task_id）',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '删除时间（UTC，逻辑删除）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_user_date` (`user_id`, `date`, `deleted_at`),
  KEY `idx_user_status` (`user_id`, `status`, `deleted_at`),
  KEY `idx_repeat_parent` (`repeat_parent_id`, `deleted_at`),
  KEY `idx_date_priority` (`date`, `priority`, `created_at`),
  KEY `idx_repeat_template` (`user_id`, `repeat_type`, `is_repeat_instance`, `deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- =====================================================
-- 3. 子任务表（sub_task）
-- =====================================================
-- 说明：存储子任务信息，挂载在父任务下
-- =====================================================

DROP TABLE IF EXISTS `sub_task`;

CREATE TABLE `sub_task` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sub_task_id` CHAR(36) NOT NULL COMMENT '子任务UUID（业务主键）',
  `parent_task_id` CHAR(36) NOT NULL COMMENT '父任务UUID',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID（冗余字段，便于查询）',
  
  -- 子任务基本信息
  `title` VARCHAR(100) NOT NULL COMMENT '子任务标题，1-50字符（UTF-8，支持emoji）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'INCOMPLETE' COMMENT '状态：INCOMPLETE（未完成）/COMPLETED（已完成）',
  
  -- 完成时间
  `completed_at` DATETIME(3) DEFAULT NULL COMMENT '完成时间（UTC）',
  
  -- 重复任务配置
  `repeat_type` VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '重复类型：NONE/DAILY/WEEKLY/MONTHLY',
  `repeat_config` JSON DEFAULT NULL COMMENT '重复配置（JSON）',
  `is_repeat_instance` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为重复子任务副本',
  `repeat_parent_id` CHAR(36) DEFAULT NULL COMMENT '重复子任务源ID',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  `deleted_at` DATETIME(3) DEFAULT NULL COMMENT '删除时间（UTC，逻辑删除）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sub_task_id` (`sub_task_id`),
  KEY `idx_parent_task` (`parent_task_id`, `deleted_at`),
  KEY `idx_user_status` (`user_id`, `status`, `deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子任务表';

-- =====================================================
-- 4. 打卡记录表（check_in）
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
-- 5. 专注会话表（focus_session）
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
-- 6. 用户专注统计表（user_focus_stats）
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
-- 7. 设备登录会话表（device_session）
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

-- =====================================================
-- 8. 会员权益表（entitlement）
-- =====================================================
-- 说明：存储用户会员权益信息
-- =====================================================

DROP TABLE IF EXISTS `entitlement`;

CREATE TABLE `entitlement` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID',
  
  -- 权益状态
  `status` VARCHAR(30) NOT NULL DEFAULT 'NOT_ENTITLED' COMMENT '权益状态：NOT_ENTITLED（未激活）/FREE_TRIAL（免费期）/MEMBER_ACTIVE（会员有效）/EXPIRED（已到期）',
  
  -- 免费期
  `trial_start_at` DATETIME(3) DEFAULT NULL COMMENT '免费期起算时间（首次微信授权登录时）',
  `trial_expire_at` DATETIME(3) DEFAULT NULL COMMENT '免费期到期时间（trial_start_at + 1个月）',
  
  -- 会员有效期
  `expire_at` DATETIME(3) DEFAULT NULL COMMENT '会员到期时间（UTC）',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status_expire` (`status`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员权益表';

-- =====================================================
-- 9. 订单表（order）
-- =====================================================
-- 说明：存储充值订单信息
-- =====================================================

DROP TABLE IF EXISTS `order`;

CREATE TABLE `order` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` CHAR(36) NOT NULL COMMENT '订单UUID（业务主键）',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID',
  
  -- 订单信息
  `plan_type` VARCHAR(20) NOT NULL COMMENT '套餐类型：MONTH（月）/QUARTER（季）/YEAR（年）',
  `amount` INT UNSIGNED NOT NULL COMMENT '金额（分）',
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  
  -- 支付信息
  `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式：WECHAT_PAY/ALIPAY',
  `payment_channel_order_id` VARCHAR(100) DEFAULT NULL COMMENT '支付渠道订单号（微信/支付宝返回）',
  `payment_params` JSON DEFAULT NULL COMMENT '支付参数（JSON）',
  
  -- 订单状态
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING（待支付）/PAID（已支付）/EXPIRED（已过期）/REFUNDED（已退款）',
  `paid_at` DATETIME(3) DEFAULT NULL COMMENT '支付时间（UTC）',
  `refunded_at` DATETIME(3) DEFAULT NULL COMMENT '退款时间（UTC）',
  `expire_at` DATETIME(3) NOT NULL COMMENT '订单过期时间（创建时间 + 15分钟）',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  UNIQUE KEY `uk_payment_channel_order` (`payment_channel_order_id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status_expire` (`status`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =====================================================
-- 10. 幂等记录表（idempotent_record）
-- =====================================================
-- 说明：存储幂等请求记录，用于接口幂等性控制
-- =====================================================

DROP TABLE IF EXISTS `idempotent_record`;

CREATE TABLE `idempotent_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `request_id` CHAR(36) NOT NULL COMMENT '请求ID（X-Request-Id）',
  `user_id` CHAR(36) NOT NULL COMMENT '用户UUID',
  
  -- 请求信息
  `api_path` VARCHAR(200) NOT NULL COMMENT 'API路径',
  `http_method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法：POST/PUT/DELETE',
  `request_body` TEXT DEFAULT NULL COMMENT '请求体（JSON）',
  
  -- 响应信息
  `response_code` INT NOT NULL COMMENT '响应状态码',
  `response_body` TEXT DEFAULT NULL COMMENT '响应体（JSON）',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  `expire_at` DATETIME(3) NOT NULL COMMENT '过期时间（创建时间 + 24小时）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_id` (`request_id`),
  KEY `idx_user_api` (`user_id`, `api_path`),
  KEY `idx_expire` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='幂等记录表';

-- =====================================================
-- 11. 系统配置表（system_config）
-- =====================================================
-- 说明：存储系统配置参数（运营后台可配置）
-- =====================================================

DROP TABLE IF EXISTS `system_config`;

CREATE TABLE `system_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值（JSON/字符串）',
  `config_type` VARCHAR(20) NOT NULL COMMENT '配置类型：STRING/JSON/INT/BOOLEAN',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
  `is_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=否，1=是',
  
  -- 公共字段
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（UTC）',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（UTC）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- =====================================================
-- 初始化系统配置数据
-- =====================================================

INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('max_device_count', '10', 'INT', '同时登录设备数上限'),
('max_task_per_day', '50', 'INT', '单日任务数上限（不含重复任务副本）'),
('max_subtask_per_task', '20', 'INT', '单个任务子任务数上限'),
('max_repeat_instances', '365', 'INT', '单个重复任务最大实例数（按需创建上限）'),
('nickname_modify_limit_days', '7', 'INT', '昵称修改限制天数'),
('nickname_modify_limit_count', '2', 'INT', '昵称修改限制次数（X天内最多Y次）'),
('free_trial_days', '30', 'INT', '免费期天数'),
('order_expire_minutes', '15', 'INT', '订单过期时间（分钟）'),
('idempotent_expire_hours', '24', 'INT', '幂等记录过期时间（小时）');

-- =====================================================
-- 索引优化说明
-- =====================================================
-- 1. user 表：
--    - uk_user_id：业务主键查询
--    - uk_email：邮箱登录查询
--    - idx_last_check_in：查询打卡统计
--
-- 2. task 表：
--    - uk_task_id：业务主键查询
--    - idx_user_date：查询指定日期任务（高频）
--    - idx_user_status：查询待执行/已完成任务
--    - idx_repeat_parent：查询/删除重复任务实例
--    - idx_date_priority：视图聚合排序
--    - idx_repeat_template：查询用户的重复模板任务（虚拟展开核心索引）
--
-- 3. sub_task 表：
--    - uk_sub_task_id：业务主键查询
--    - idx_parent_task：查询父任务的子任务（高频）
--    - idx_user_status：查询用户子任务状态
--
-- 4. check_in 表：
--    - uk_user_date：打卡幂等、查询当日打卡记录
--    - idx_date：统计某日打卡用户数
--
-- 5. focus_session 表：
--    - uk_session_id：业务主键查询
--    - idx_user_status：查询进行中的会话
--    - idx_user_counted：统计总专注时长
--
-- 6. device_session 表：
--    - uk_access_token：token验证（高频）
--    - idx_user_status：查询用户设备列表
--    - idx_refresh_token：刷新token查询
--
-- 7. order 表：
--    - uk_payment_channel_order：支付回调幂等
--    - idx_user_status：查询用户订单
--    - idx_status_expire：定时任务关闭过期订单
--
-- 8. idempotent_record 表：
--    - uk_request_id：幂等查询（高频）
--    - idx_expire：定时任务清理过期记录
-- =====================================================

-- =====================================================
-- 数据一致性策略
-- =====================================================
-- 1. 强一致性（事务保证）：
--    - 用户注册/登录（user + entitlement + device_session）
--    - 订单支付（order + entitlement）
--    - 任务完成/反完成（task + sub_task 父子联动）
--    - 打卡（user + check_in）
--
-- 2. 最终一致性：
--    - 重复任务实例按需创建（用户操作时同步创建）
--    - 专注统计汇总（user_focus_stats，异步或定时更新）
--    - 幂等记录清理（定时任务）
--    - 过期订单关闭（定时任务）
--
-- 3. 冗余字段：
--    - sub_task.user_id：冗余自父任务，便于直接查询
--    - check_in.consecutive_days：打卡时的快照，便于追溯
--    - user_focus_stats：汇总表，避免每次聚合计算
-- =====================================================

-- =====================================================
-- 定时任务需求
-- =====================================================
-- 2. 每日 01:00（UTC）：
--    - 清理过期幂等记录（expire_at < now）
--    - 关闭过期订单（status=PENDING AND expire_at < now）
--
-- 3. 重复任务说明：
--    - 采用虚拟展开方案，无需定时任务预生成副本
--    - 用户操作时按需实例化（POST /api/v1/tasks/{虚拟ID}/materialize）
--
-- 4. 每周一 00:00（UTC）：
--    - 重置昵称修改限制计数器（7天窗口滚动）
-- =====================================================

-- =====================================================
-- 文档结束
-- =====================================================
