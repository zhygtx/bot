-- 快速重建：先关闭外键检查，脚本里每个表头都有 DROP TABLE IF EXISTS，可直接整库重建；最后再恢复外键检查。
SET FOREIGN_KEY_CHECKS = 0;

-- 用户信息表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
`id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '用户 ID',
`name` VARCHAR(255) NOT NULL COMMENT '用户名',
`account` VARCHAR(100) NOT NULL COMMENT '用户账号',
`pwd` VARCHAR(255) NOT NULL COMMENT '密码',
`QQ` BIGINT COMMENT 'QQ 号',
`email` VARCHAR(255) COMMENT '用户邮箱',
`user_role` VARCHAR(50) COMMENT '用户权限',
`create_time` DATETIME NOT NULL COMMENT '创建时间',
`update_time` DATETIME NOT NULL COMMENT '最后修改时间',
INDEX `idx_account` (`account`),
INDEX `idx_QQ` (`QQ`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 用户自定义主题表
DROP TABLE IF EXISTS `user_theme`;
CREATE TABLE `user_theme` (
`id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '主题 ID',
`user_id` VARCHAR(36) NOT NULL COMMENT '主题所属用户 ID',
`name` VARCHAR(100) NOT NULL COMMENT '主题名称',
`mode` VARCHAR(20) NOT NULL COMMENT '主题模式：light/dark',
`tokens` JSON NOT NULL COMMENT '主题 CSS 变量令牌快照',
`create_time` DATETIME NOT NULL COMMENT '创建时间',
`update_time` DATETIME NOT NULL COMMENT '更新时间',
FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
INDEX `idx_user_id` (`user_id`),
INDEX `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户自定义主题表';

-- 用户当前主题设置表
DROP TABLE IF EXISTS `user_theme_setting`;
CREATE TABLE `user_theme_setting` (
`user_id` VARCHAR(36) NOT NULL COMMENT '用户 ID',
`mode` VARCHAR(20) NOT NULL COMMENT '主题模式：light/dark',
`active_theme_type` VARCHAR(20) NOT NULL COMMENT '当前主题类型：BUILTIN/CUSTOM',
`active_theme_key` VARCHAR(100) NOT NULL COMMENT '当前主题键：内置主题 key 或 user_theme.id',
`update_time` DATETIME NOT NULL COMMENT '更新时间',
PRIMARY KEY (`user_id`, `mode`),
FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户当前主题设置表';

-- 机器人信息表
DROP TABLE IF EXISTS `bot`;
CREATE TABLE `bot` (
`id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '机器人 ID',
`name` VARCHAR(255) COMMENT '机器人名称',
`bot_qq` BIGINT NOT NULL COMMENT '机器人 QQ',
`user_id` VARCHAR(36) NOT NULL COMMENT '机器人所有者 ID',
`token` VARCHAR(255) COMMENT '机器人Token',
`path_suffix` VARCHAR(255) COMMENT '机器人路径后缀',
`is_online` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '机器人是否在线',
`online_since` BIGINT COMMENT '本次上线时间戳（毫秒），离线时置空',
FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
INDEX `idx_bot_qq` (`bot_qq`),
INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='机器人信息表';

-- 容器信息表
DROP TABLE IF EXISTS `docker`;
CREATE TABLE `docker` (
`container_id` VARCHAR(100) NOT NULL PRIMARY KEY COMMENT '容器 ID',
`name` VARCHAR(255) NOT NULL COMMENT '容器名称',
`user_id` VARCHAR(36) NOT NULL COMMENT '容器所属用户',
`bot_qq` BIGINT COMMENT '容器所属机器人 QQ',
`port` INT COMMENT '容器外部映射端口',
`token` VARCHAR(255) COMMENT '容器 token',
`create_time` DATETIME NOT NULL COMMENT '创建时间',
`update_time` DATETIME NOT NULL COMMENT '更新时间',
FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
INDEX `idx_user_id` (`user_id`),
INDEX `idx_bot_qq` (`bot_qq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='容器信息表';


# ----------------------------------------------------------------------------------------------------------------------

-- 插件信息表
DROP TABLE IF EXISTS `plugin_info`;
CREATE TABLE `plugin_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '插件 id',
  `name` VARCHAR(255) NOT NULL COMMENT '插件名称',
  `description` TEXT COMMENT '插件描述',
  `author_id` VARCHAR(36) NOT NULL COMMENT '插件作者 (即 userId)',
  `latest_version` VARCHAR(50) COMMENT '最新版本号',
  `version_count` INT DEFAULT 0 COMMENT '版本总数',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `is_public` TINYINT DEFAULT 0 not null COMMENT '是否公开',
  FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件信息表';

-- 插件版本表
DROP TABLE IF EXISTS `plugin_version`;
CREATE TABLE `plugin_version` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '版本ID',
  `plugin_id` VARCHAR(36) NOT NULL COMMENT '插件ID',
  `version` VARCHAR(50) NOT NULL COMMENT '版本号',
  `path` VARCHAR(500) NOT NULL COMMENT '插件文件路径',
  `file_size` BIGINT COMMENT '文件大小',
  `file_md5` VARCHAR(32) COMMENT '文件MD5',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `changelog` TEXT COMMENT '版本变更说明',
  `entity_package` VARCHAR(255) COMMENT '插件实体类包名',
  `method_package` VARCHAR(255) COMMENT '插件方法类包名',
  `compatible_version` TEXT COMMENT '此版本所兼容的版本（JSON）',
  FOREIGN KEY (`plugin_id`) REFERENCES `plugin_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件版本表';

-- 实体类信息表
DROP TABLE IF EXISTS `entity_info`;
CREATE TABLE `entity_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '实体类id',
  `description` TEXT COMMENT '实体类描述',
  `name` VARCHAR(255) NOT NULL COMMENT '实体类简写名称',
  `plugin_version_id` VARCHAR(36) NOT NULL COMMENT '实体类所属插件版本id',
  `entity_name` VARCHAR(255) NOT NULL COMMENT '实体全限定名',
  FOREIGN KEY (`plugin_version_id`) REFERENCES `plugin_version` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体类信息表';

-- 属性信息表
DROP TABLE IF EXISTS `attribute`;
CREATE TABLE `attribute` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '属性id',
  `description` TEXT COMMENT '属性描述',
  `entity_info_id` VARCHAR(36) NOT NULL COMMENT '属性所属实体类id',
  `type` VARCHAR(255) NOT NULL COMMENT '属性类型',
  `name` VARCHAR(255) NOT NULL COMMENT '属性名称',
  FOREIGN KEY (`entity_info_id`) REFERENCES `entity_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性信息表';

-- 方法类信息表
DROP TABLE IF EXISTS `method_class_info`;
CREATE TABLE `method_class_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '方法类id',
  `description` TEXT COMMENT '类描述',
  `plugin_version_id` VARCHAR(36) NOT NULL COMMENT '所属插件版本id',
  `class_name` VARCHAR(255) NOT NULL COMMENT '类全限定名',
  `simple_class_name` VARCHAR(255) NOT NULL COMMENT '简单类名',
  `package_name` VARCHAR(255) NOT NULL COMMENT '包名',
  FOREIGN KEY (`plugin_version_id`) REFERENCES `plugin_version` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方法类信息表';

-- 方法信息表
DROP TABLE IF EXISTS `method_info`;
CREATE TABLE `method_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '方法id',
  `description` TEXT COMMENT '方法描述',
  `method_class_id` VARCHAR(36) NOT NULL COMMENT '方法所属方法类ID',
  `name` VARCHAR(255) NOT NULL COMMENT '方法名',
  `return_type` VARCHAR(255) NOT NULL COMMENT '方法返回值类型',
  `return_description` TEXT COMMENT '方法返回值描述',
  FOREIGN KEY (`method_class_id`) REFERENCES `method_class_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方法信息表';

-- 参数信息表
DROP TABLE IF EXISTS `parameter_info`;
CREATE TABLE `parameter_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '参数id',
  `description` TEXT COMMENT '参数描述',
  `method_id` VARCHAR(36) NOT NULL COMMENT '参数所属方法id',
  `name` VARCHAR(255) NOT NULL COMMENT '参数名',
  `type` VARCHAR(255) NOT NULL COMMENT '参数类型',
  `nullable` TINYINT DEFAULT 0 COMMENT '是否可空',
  `order` INT NOT NULL COMMENT '参数顺序',
  FOREIGN KEY (`method_id`) REFERENCES `method_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数信息表';

# -------------------------------------------------------------------------------------------------------------

-- 工作流表
DROP TABLE IF EXISTS `workflow`;
CREATE TABLE `workflow` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '工作流 ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '工作流创建者 ID',
  `name` VARCHAR(255) NOT NULL COMMENT '工作流名称',
  `enabled` TINYINT DEFAULT 1 COMMENT '工作流是否启用',
  `available` TINYINT DEFAULT 1 COMMENT '工作流是否可用',
  `disable_reason` VARCHAR(255) COMMENT '工作流禁用原因',
  `trigger_key` VARCHAR(255) COMMENT '触发键（botEvent:{botQQ}:{EventType} 或 schedule:{seconds}）',
  `definition` LONGTEXT NOT NULL COMMENT '工作流定义（JSON：节点、连线、画布视图）',
  `create_time` DATETIME NOT NULL COMMENT '工作流创建时间',
  `update_time` DATETIME NOT NULL COMMENT '工作流更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  INDEX `idx_workflow_user` (`user_id`),
  INDEX `idx_workflow_trigger_key` (`trigger_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流表';

-- 插件数据存储表
DROP TABLE IF EXISTS `plugin_data`;
CREATE TABLE `plugin_data` (
    `id` integer NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    `user_id` VARCHAR(36) NOT NULL COMMENT '用户 ID',
    `plugin_id` VARCHAR(36) NOT NULL COMMENT '插件 ID',
    `data_index` VARCHAR(255) COMMENT '数据索引/键名',
    `data` text COMMENT '存储的数据（JSON 格式）',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`plugin_id`) REFERENCES `plugin_info` (`id`) ON DELETE CASCADE,
    INDEX idx_user_id (`user_id`),
    INDEX idx_plugin_id (`plugin_id`),
    INDEX idx_data_index (`data_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件数据存储表';

# --------------------------------------------------------------------------------------------------------

-- 工作流执行记录表
DROP TABLE IF EXISTS `workflow_execution`;
CREATE TABLE `workflow_execution` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '执行记录ID（自增主键）',
  `workflow_id` VARCHAR(36) NOT NULL COMMENT '工作流ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
  `trigger_key` VARCHAR(255) COMMENT '触发键',
  `workflow_name` VARCHAR(255) COMMENT '工作流名称',
  `status` VARCHAR(16) NOT NULL COMMENT '状态：SUCCESS/FAILED',
  `start_time` BIGINT COMMENT '工作流执行开始时间戳',
  `end_time` BIGINT COMMENT '工作流执行结束时间戳',
  `duration_ms` BIGINT COMMENT '工作流执行耗时（毫秒）',
  `expected_node_count` INT COMMENT '工作流预计执行节点个数',
  `actual_node_count` INT COMMENT '工作流实际执行节点个数',
  `error_message` TEXT COMMENT '工作流报错信息',
  `trace` LONGTEXT NOT NULL COMMENT '节点执行轨迹（JSON）',
  FOREIGN KEY (`workflow_id`) REFERENCES `workflow` (`id`) ON DELETE CASCADE,
  INDEX `idx_user_id_start_time` (`user_id`, `start_time`),
  INDEX `idx_workflow_id_start_time` (`workflow_id`, `start_time`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流执行记录表';

# ----------------------------------------------------------------------------------------------------------------

-- 工作流执行日志每日统计表（凌晨定时任务把前一天执行记录聚合到这里，原始明细清理后统计仍可长期保留）
DROP TABLE IF EXISTS `workflow_execution_daily_stat`;
CREATE TABLE `workflow_execution_daily_stat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '统计记录ID（自增主键）',
  `stat_date` DATE NOT NULL COMMENT '统计日期（服务器本地日期）',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
  `workflow_id` VARCHAR(36) NOT NULL COMMENT '工作流ID',
  `workflow_name` VARCHAR(255) NOT NULL COMMENT '工作流名称快照，改名后历史统计仍显示当时的名称',
  `execute_count` INT NOT NULL DEFAULT 0 COMMENT '当日执行次数',
  `success_count` INT NOT NULL DEFAULT 0 COMMENT '当日成功次数',
  `failed_count` INT NOT NULL DEFAULT 0 COMMENT '当日失败次数',
  `total_duration_ms` BIGINT NOT NULL DEFAULT 0 COMMENT '当日总耗时（毫秒）',
  `avg_duration_ms` BIGINT NOT NULL DEFAULT 0 COMMENT '当日平均耗时（毫秒）',
  `max_duration_ms` BIGINT NOT NULL DEFAULT 0 COMMENT '当日最大耗时（毫秒）',
  `min_duration_ms` BIGINT NOT NULL DEFAULT 0 COMMENT '当日最小耗时（毫秒）',
  `total_actual_node_count` BIGINT NOT NULL DEFAULT 0 COMMENT '当日实际执行节点总数',
  `avg_actual_node_count` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '当日平均实际执行节点数',
  `last_start_time` BIGINT NOT NULL DEFAULT 0 COMMENT '当日最后一次执行开始时间戳（毫秒）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`workflow_id`) REFERENCES `workflow` (`id`) ON DELETE CASCADE,
  UNIQUE KEY `uk_stat_date_workflow` (`stat_date`, `user_id`, `workflow_id`),
  INDEX `idx_stat_user_date` (`user_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流执行日志每日统计表';

# --------------------------------------------------------------------------------------------------------
-- ============================================
-- AI Chat Message 消息表
-- ============================================
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message`(
    `id`                 VARCHAR(36)  NOT NULL COMMENT '记录主键（UUID，每条记录唯一）',
    `conversation_id`    VARCHAR(36)  NOT NULL COMMENT '会话 ID（同一对话中所有记录共享此值）',
    `plugin_id`          VARCHAR(36)  DEFAULT NULL COMMENT '关联的插件 ID（首次生成时为 null）',
    `user_id`            VARCHAR(36)  NOT NULL COMMENT '创建者用户 ID',
    `round`              INT          NOT NULL DEFAULT 1 COMMENT '对话轮次，从 1 开始',
    `user_message`       TEXT         DEFAULT NULL COMMENT '用户指令文本',
    `message_parts`      LONGTEXT     DEFAULT NULL COMMENT 'AI 回复分段内容（JSON 数组，用于 Markdown 文本中穿插工具调用卡片）',
    `prompt_tokens`      INT          DEFAULT NULL COMMENT '上次生成请求的实际 prompt token 数（OpenAI prompt_tokens / Anthropic input_tokens）',
    `status`             VARCHAR(32)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT-未发布, PUBLISHED-已发布, PUBLISHED_DRAFT-更新未发布',
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pom`                TEXT         DEFAULT NULL COMMENT '代码依赖（pom.xml 内容）',
    `plugin_name`        VARCHAR(255) DEFAULT NULL COMMENT '插件名称',
    `plugin_description` TEXT         DEFAULT NULL COMMENT '插件介绍',
    `version`            VARCHAR(64)  DEFAULT NULL COMMENT '版本号',
    `is_public`          TINYINT(1)   DEFAULT 0 COMMENT '是否公开: 0-否, 1-是',
    `changelog`          TEXT         DEFAULT NULL COMMENT '版本变更说明',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_plugin_id` (`plugin_id`),
    INDEX `idx_user_id` (`user_id`),
    CONSTRAINT `fk_message_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AI 聊天消息表';

-- ============================================
-- Code 代码表（子表）
-- ============================================
DROP TABLE IF EXISTS `code`;
CREATE TABLE `code`(
    `id`          VARCHAR(36)  NOT NULL COMMENT '代码 ID',
    `message_id`  VARCHAR(36)  NOT NULL COMMENT '消息 ID（外键，关联 ai_chat_message.id）',
    `path`        VARCHAR(512) DEFAULT NULL COMMENT '代码路径',
    `content`     LONGTEXT     DEFAULT NULL COMMENT '代码内容',
    `description` TEXT         DEFAULT NULL COMMENT '代码介绍',
    PRIMARY KEY (`id`),
    INDEX `idx_message_id` (`message_id`),
    CONSTRAINT `fk_code_message_id`
        FOREIGN KEY (`message_id`) REFERENCES `ai_chat_message` (`id`)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='代码表';

-- ============================================
-- AI Message Summary 消息摘要表（上下文压缩，只增不删）
-- ============================================
DROP TABLE IF EXISTS `ai_message_summary`;
CREATE TABLE `ai_message_summary`(
    `id`              VARCHAR(36)  NOT NULL COMMENT '记录主键（UUID，每条记录唯一）',
    `conversation_id` VARCHAR(36)  NOT NULL COMMENT '会话 ID（同一对话中所有记录共享此值）',
    `user_id`         VARCHAR(36)  NOT NULL COMMENT '创建者用户 ID',
    `summary_round`   INT          NOT NULL COMMENT '被压缩的最后一轮',
    `message_id`      VARCHAR(36)  NOT NULL COMMENT '指向 summary_round 那一轮的消息 ID（外键，关联 ai_chat_message.id）',
    `summary_content` LONGTEXT     NOT NULL COMMENT '压缩结果 JSON',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_round` (`conversation_id`, `summary_round`),
    INDEX `idx_message_id` (`message_id`),
    CONSTRAINT `fk_summary_message_id`
        FOREIGN KEY (`message_id`) REFERENCES `ai_chat_message` (`id`)
            ON DELETE CASCADE
            ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='AI 消息摘要表';

-- AI 配置信息表
DROP TABLE IF EXISTS `user_ai_config`;
CREATE TABLE `user_ai_config` (
 `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '主键ID',
 `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
 `api_provider` VARCHAR(50) NOT NULL COMMENT 'API接口类型',
 `base_url` VARCHAR(500) NOT NULL COMMENT 'API接口基础URL',
 `api_key` VARCHAR(255) NOT NULL COMMENT 'API密钥',
 `model` VARCHAR(100) NOT NULL COMMENT '模型名称',
 FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
 INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI配置信息表';

SET FOREIGN_KEY_CHECKS = 1;
