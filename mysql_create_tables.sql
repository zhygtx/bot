-- 用户信息表
CREATE TABLE `user` (
`id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '用户 ID',
`name` VARCHAR(255) NOT NULL COMMENT '用户名',
`account` VARCHAR(100) NOT NULL COMMENT '用户账号',
`pwd` VARCHAR(255) NOT NULL COMMENT '密码',
`QQ` BIGINT COMMENT 'QQ 号',
`email` VARCHAR(255) COMMENT '用户邮箱',
`bot_qq` BIGINT COMMENT '用户所拥有的机器人的 ID',
`user_role` VARCHAR(50) COMMENT '用户权限',
`create_time` DATETIME NOT NULL COMMENT '创建时间',
`update_time` DATETIME NOT NULL COMMENT '最后修改时间',
INDEX `idx_account` (`account`),
INDEX `idx_QQ` (`QQ`),
INDEX `idx_botQQ` (`bot_qq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 机器人信息表
CREATE TABLE `bot` (
`id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '机器人 ID',
`name` VARCHAR(255) NOT NULL COMMENT '机器人名称',
`bot_qq` BIGINT NOT NULL COMMENT '机器人 QQ',
`user_id` VARCHAR(36) NOT NULL COMMENT '机器人所有者 ID',
`is_online` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '机器人是否在线',
FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
INDEX `idx_bot_qq` (`bot_qq`),
INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='机器人信息表';

-- 容器信息表
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

-- 插件信息表
CREATE TABLE `plugin_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '插件 id',
  `name` VARCHAR(255) NOT NULL COMMENT '插件名称',
  `description` TEXT COMMENT '插件描述',
  `author_id` VARCHAR(36) NOT NULL COMMENT '插件作者 (即 userId)',
  `author_name` VARCHAR(255) not null COMMENT '作者昵称',
  `latest_version` VARCHAR(50) COMMENT '最新版本号',
  `version_count` INT DEFAULT 0 COMMENT '版本总数',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `is_public` TINYINT DEFAULT 0 not null COMMENT '是否公开',
  FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件信息表';

-- 插件版本表
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
CREATE TABLE `entity_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '实体类id',
  `description` TEXT COMMENT '实体类描述',
  `name` VARCHAR(255) NOT NULL COMMENT '实体类简写名称',
  `plugin_version_id` VARCHAR(36) NOT NULL COMMENT '实体类所属插件版本id',
  `entity_name` VARCHAR(255) NOT NULL COMMENT '实体全限定名',
  FOREIGN KEY (`plugin_version_id`) REFERENCES `plugin_version` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体类信息表';

-- 属性信息表
CREATE TABLE `attribute` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '属性id',
  `description` TEXT COMMENT '属性描述',
  `entity_info_id` VARCHAR(36) NOT NULL COMMENT '属性所属实体类id',
  `type` VARCHAR(255) NOT NULL COMMENT '属性类型',
  `name` VARCHAR(255) NOT NULL COMMENT '属性名称',
  FOREIGN KEY (`entity_info_id`) REFERENCES `entity_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性信息表';

-- 方法类信息表
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
CREATE TABLE `parameter_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '参数id',
  `description` TEXT COMMENT '参数描述',
  `method_id` VARCHAR(36) NOT NULL COMMENT '参数所属方法id',
  `name` VARCHAR(255) NOT NULL COMMENT '参数名',
  `type` VARCHAR(255) NOT NULL COMMENT '参数类型',
  `order` INT NOT NULL COMMENT '参数顺序',
  FOREIGN KEY (`method_id`) REFERENCES `method_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数信息表';

# -------------------------------------------------------------------------------------------------------------

-- 工作流信息表
CREATE TABLE `workflow_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '工作流 ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '工作流创建者 ID',
  `author_name` VARCHAR(255) not null comment '作者昵称',
  `name` VARCHAR(255) NOT NULL COMMENT '工作流名称',
  `available` TINYINT DEFAULT 1 COMMENT '工作流是否可用',
  `disable_reason` VARCHAR(255) COMMENT '工作流禁用原因',
  `create_time` DATETIME NOT NULL COMMENT '工作流创建时间',
  `update_time` DATETIME NOT NULL COMMENT '工作流更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流信息表';

-- 工作流节点表
CREATE TABLE `node` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '节点 ID',
  `x` INT COMMENT '节点位置（水平位置）',
  `y` INT COMMENT '节点位置（垂直位置）',
  `workflow_id` VARCHAR(36) NOT NULL COMMENT '所属工作流 ID',
  `plugin_id` VARCHAR(36) COMMENT '插件 ID（仅 pluginMethod 节点使用）',
  `plugin_version_id` VARCHAR(36) COMMENT '插件版本 ID（仅 pluginMethod 节点使用）',
  `method_class_id` VARCHAR(36) COMMENT '方法类 ID（仅 pluginMethod 节点使用）',
  `method_id` VARCHAR(36) COMMENT '方法 ID（仅 pluginMethod 节点使用）',
  `node_type` ENUM('botEvent','pluginMethod','botAction') NOT NULL COMMENT '节点类型',
  `event_type` VARCHAR(255) COMMENT '事件类型（仅 botEvent 节点使用）',
  `bot_qq` BIGINT COMMENT 'Bot QQ 号（仅 botEvent 节点使用）',
  `bot_action_name` VARCHAR(255) COMMENT '动作名称（仅 botAction 节点使用）',
  `bot_event_name` VARCHAR(255) COMMENT '事件名称（仅 botEvent 节点使用）',
  `scheduled_time` INT COMMENT '定时时间（秒）（仅 scheduledEvent 节点使用）',
  `in_degree` INT NOT NULL DEFAULT 0 COMMENT '入度',
  FOREIGN KEY (`workflow_id`) REFERENCES `workflow_info` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`plugin_id`) REFERENCES `plugin_info` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`plugin_version_id`) REFERENCES `plugin_version` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`method_class_id`) REFERENCES `method_class_info` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`method_id`) REFERENCES `method_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流节点表';

-- 节点前置关系表（处理多对多关系）
CREATE TABLE `node_pre_relation` (
  `id` integer NOT NULL auto_increment PRIMARY KEY COMMENT '关系ID',
  `node_id` VARCHAR(36) NOT NULL COMMENT '节点ID',
  `pre_node_id` VARCHAR(36) NOT NULL COMMENT '前置节点ID',
  FOREIGN KEY (`node_id`) REFERENCES `node` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`pre_node_id`) REFERENCES `node` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点前置关系表';

-- 节点后置关系表（处理多对多关系）
CREATE TABLE `node_next_relation` (
  `id` integer NOT NULL auto_increment PRIMARY KEY COMMENT '关系ID',
  `node_id` VARCHAR(36) NOT NULL COMMENT '节点ID',
  `next_node_id` VARCHAR(36) NOT NULL COMMENT '后置节点ID',
  FOREIGN KEY (`node_id`) REFERENCES `node` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`next_node_id`) REFERENCES `node` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点后置关系表';

-- 条件表
CREATE TABLE `condition` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '条件ID',
  `node_id` VARCHAR(36) NOT NULL COMMENT '所判断的节点ID（插件节点）',
  `true_action` ENUM('CONTINUE', 'BREAK', 'END') NOT NULL COMMENT '插件返回true时执行内容',
  `false_action` ENUM('CONTINUE', 'BREAK', 'END') NOT NULL COMMENT '插件返回false时执行内容',
  FOREIGN KEY (`node_id`) REFERENCES `node` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='条件表';

-- 数据映射表
CREATE TABLE `data_map` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '映射关系ID',
  `node_id` VARCHAR(36) NOT NULL COMMENT '所属节点ID',
  `source_node_id` VARCHAR(36) NOT NULL COMMENT '源数据所属节点',
  `source_path` VARCHAR(500) NOT NULL COMMENT '源数据字段名称(支持嵌套如：value.id，无嵌套直接映射时为value)',
  `target_param_name` VARCHAR(255) NOT NULL COMMENT '目标参数名称',
  `param_index` INT COMMENT '方法的第几个参数',
  `target_path` VARCHAR(500) COMMENT '目标参数字段名(支持嵌套如：user.id，无嵌套直接映射时为user即和参数名相同)',
  `source_type` VARCHAR(255) NOT NULL COMMENT '源数据字段类型',
  `target_type` VARCHAR(255) NOT NULL COMMENT '目标属性方法参数字段类型',
  FOREIGN KEY (`node_id`) REFERENCES `node` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`source_node_id`) REFERENCES `node` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据映射表';

-- 节点默认值表
CREATE TABLE `node_defaults` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '默认值ID',
  `node_id` VARCHAR(36) NOT NULL COMMENT '节点ID',
  `param_index` INT NOT NULL COMMENT '方法的第几个参数',
  `param_name` VARCHAR(255) COMMENT '方法参数名称',
  `field_path` VARCHAR(500) COMMENT '字段名(支持嵌套如：user.id，无嵌套直接映射时为user即和参数名相同)',
  `default_value` TEXT COMMENT '默认值',
  `default_value_type` ENUM('String', 'Integer', 'Double', 'Boolean', 'Long') NOT NULL COMMENT '默认值类型',
  FOREIGN KEY (`node_id`) REFERENCES `node` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点默认值表';

-- 插件数据存储表
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

-- 工作流日志表
CREATE TABLE `workflow_log` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '工作流日志ID',
  `workflow_id` VARCHAR(36) NOT NULL COMMENT '工作流ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID',
  `expected_node_count` INT COMMENT '工作流预计执行节点个数',
  `actual_node_count` INT COMMENT '工作流实际执行节点个数',
  `execution_time` BIGINT COMMENT '工作流执行耗时（毫秒）',
  `start_time` BIGINT COMMENT '工作流执行开始时间戳',
  `initial_context` TEXT COMMENT '工作流初始上下文（JSON格式）',
  `workflow_name` VARCHAR(255) COMMENT '工作流名称',
  INDEX `idx_workflow_id` (`workflow_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流日志表';

-- 节点日志表
CREATE TABLE `node_log` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '节点日志ID',
  `workflow_log_id` VARCHAR(36) NOT NULL COMMENT '工作流日志ID',
  `method_id` VARCHAR(36) COMMENT '节点使用的插件方法ID',
  `node_id` VARCHAR(36) NOT NULL COMMENT '节点ID',
  `execution_time` BIGINT COMMENT '节点执行耗时（毫秒）',
  `order` INT COMMENT '节点执行次序',
  `input` TEXT COMMENT '节点输入内容（JSON格式）',
  `output` TEXT COMMENT '节点输出内容（JSON格式）',
  `method_name` VARCHAR(255) COMMENT '节点方法名称',
  `method_description` TEXT COMMENT '节点方法描述',
  INDEX `idx_workflow_log_id` (`workflow_log_id`),
  INDEX `idx_method_id` (`method_id`),
  INDEX `idx_order` (`order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点日志表';
