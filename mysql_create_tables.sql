-- 插件信息表
CREATE TABLE `plugin_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '插件id',
  `name` VARCHAR(255) NOT NULL COMMENT '插件名称',
  `description` TEXT COMMENT '插件描述',
  `author_id` VARCHAR(36) NOT NULL COMMENT '插件作者（即userId）',
  'author_name' VARCHAR(255) not null COMMENT '作者昵称',
  `latest_version` VARCHAR(50) COMMENT '最新版本号',
  `version_count` INT DEFAULT 0 COMMENT '版本总数',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `is_public` TINYINT DEFAULT 0 not null COMMENT '是否公开'
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
  `attributes` TEXT COMMENT '实体类属性信息(JSON格式)',
  FOREIGN KEY (`plugin_version_id`) REFERENCES `plugin_version` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实体类信息表';

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
  FOREIGN KEY (`method_class_id`) REFERENCES `method_class_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='方法信息表';

-- 参数信息表
CREATE TABLE `parameter_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '参数id',
  `description` TEXT COMMENT '参数描述',
  `method_id` VARCHAR(36) NOT NULL COMMENT '参数所属方法id',
  `name` VARCHAR(255) NOT NULL COMMENT '参数名',
  `type` VARCHAR(255) NOT NULL COMMENT '参数类型',
  FOREIGN KEY (`method_id`) REFERENCES `method_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数信息表';

# -------------------------------------------------------------------------------------------------------------

-- 工作流信息表
CREATE TABLE `workflow_info` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '工作流ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '工作流创建者ID',
  `name` VARCHAR(255) NOT NULL COMMENT '工作流名称',
  `description` TEXT COMMENT '工作流描述',
  `create_time` DATETIME NOT NULL COMMENT '工作流创建时间',
  `update_time` DATETIME NOT NULL COMMENT '工作流更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流信息表';

-- 工作流节点表
CREATE TABLE `node` (
  `id` VARCHAR(36) NOT NULL PRIMARY KEY COMMENT '节点ID',
  `x` INT COMMENT '节点位置（水平位置）',
  `y` INT COMMENT '节点位置（垂直位置）',
  `workflow_id` VARCHAR(36) NOT NULL COMMENT '所属工作流ID',
  `plugin_id` VARCHAR(36) NOT NULL COMMENT '插件ID',
  `plugin_version_id` VARCHAR(36) NOT NULL COMMENT '插件版本ID',
  `method_class_id` VARCHAR(36) NOT NULL COMMENT '方法类ID',
  `method_id` VARCHAR(36) NOT NULL COMMENT '方法ID',
  `in_degree` INT NOT NULL DEFAULT 0 COMMENT '入度',
  FOREIGN KEY (`plugin_id`) REFERENCES `plugin_info` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`workflow_id`) REFERENCES `workflow_info` (`id`) ON DELETE CASCADE,
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
  `workflow_id` VARCHAR(36) NOT NULL COMMENT '所属工作流ID',
  `node_id` VARCHAR(36) NOT NULL COMMENT '所判断的数据产生者节点ID',
  `field_name` VARCHAR(255) NOT NULL COMMENT '判断的数据字段名称',
  `preset_content` TEXT COMMENT '预设内容（位于比较右侧）',
  `content_type` ENUM('STRING', 'NUMBER', 'BOOLEAN') NOT NULL COMMENT '预设内容类型',
  `action` ENUM('CONTINUE', 'BREAK', 'END') NOT NULL COMMENT '满足条件时执行内容',
  `else_action` ENUM('CONTINUE', 'BREAK', 'END') NOT NULL COMMENT '不满足条件时执行内容',
  `operator` ENUM('EQUALS', 'NOT_EQUALS', 'GREATER_THAN', 'GREATER_THAN_OR_EQUALS', 'LESS_THAN', 'LESS_THAN_OR_EQUALS', 'CONTAINS', 'NOT_CONTAINS', 'REGEX', 'IS_NULL', 'IS_NOT_NULL') NOT NULL COMMENT '判断条件操作符',
  FOREIGN KEY (`workflow_id`) REFERENCES `workflow_info` (`id`) ON DELETE CASCADE,
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
