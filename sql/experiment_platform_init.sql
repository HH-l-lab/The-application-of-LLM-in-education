-- 实验平台及创作者生态系统 SQL 初始化脚本
-- ----------------------------

-- ----------------------------
-- 1. 多级教材目录表 sys_catalog
-- ----------------------------
DROP TABLE IF EXISTS `sys_catalog`;
CREATE TABLE `sys_catalog` (
  `catalog_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '目录ID',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父目录ID',
  `ancestors` varchar(50) DEFAULT '' COMMENT '祖级列表',
  `catalog_name` varchar(50) DEFAULT '' COMMENT '目录名称',
  `order_num` int(4) DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) DEFAULT '0' COMMENT '目录状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`catalog_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教材目录表';

-- ----------------------------
-- 2. 在线课程资源表 biz_course
-- ----------------------------
DROP TABLE IF EXISTS `biz_course`;
CREATE TABLE `biz_course` (
  `course_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_title` varchar(255) NOT NULL COMMENT '课程标题',
  `creator_id` bigint(20) NOT NULL COMMENT '上传者(创作者)ID，关联sys_user',
  `video_url` varchar(500) DEFAULT NULL COMMENT '流媒体视频播放URL',
  `course_type` char(1) DEFAULT '0' COMMENT '课程类型（0免费 1付费）',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
  `cover_image` varchar(500) DEFAULT NULL COMMENT '封面图片URL',
  `audit_status` char(1) DEFAULT '0' COMMENT '审核状态（0待审核 1已发布 2驳回）',
  `play_count` bigint(20) DEFAULT '0' COMMENT '播放次数',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线课程资源表';

-- ----------------------------
-- 3. 推荐器材表 biz_equipment
-- ----------------------------
DROP TABLE IF EXISTS `biz_equipment`;
CREATE TABLE `biz_equipment` (
  `equipment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '器材ID',
  `equipment_name` varchar(100) NOT NULL COMMENT '器材名称',
  `specifications` varchar(255) DEFAULT '' COMMENT '规格型号',
  `usage_instructions` text COMMENT '使用说明及注意事项',
  `reference_price` decimal(10,2) DEFAULT '0.00' COMMENT '参考价格',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验推荐器材表';

-- ----------------------------
-- 4. 资源关联表 Course<->Catalog, Course<->Equipment
-- ----------------------------
DROP TABLE IF EXISTS `biz_course_catalog`;
CREATE TABLE `biz_course_catalog` (
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `catalog_id` bigint(20) NOT NULL COMMENT '目录ID',
  PRIMARY KEY (`course_id`,`catalog_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程和目录关联表';

DROP TABLE IF EXISTS `biz_course_equipment`;
CREATE TABLE `biz_course_equipment` (
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `equipment_id` bigint(20) NOT NULL COMMENT '器材ID',
  PRIMARY KEY (`course_id`,`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程和器材关联表';

-- ----------------------------
-- 5. 实验任务配置表 biz_task_config
-- ----------------------------
DROP TABLE IF EXISTS `biz_task_config`;
CREATE TABLE `biz_task_config` (
  `task_config_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务配置ID',
  `course_id` bigint(20) NOT NULL COMMENT '关联的课程ID',
  `form_schema` json DEFAULT NULL COMMENT 'JSON格式的实验表单动态结构',
  `llm_prompt_template` text COMMENT '用于AI预检的提示词模板',
  `word_template_path` varchar(500) DEFAULT NULL COMMENT '生成Word报告的底版路径',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`task_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验任务配置表';

-- ----------------------------
-- 6. 学生实验过程记录表 biz_experiment_record
-- ----------------------------
DROP TABLE IF EXISTS `biz_experiment_record`;
CREATE TABLE `biz_experiment_record` (
  `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID(sys_user)',
  `course_id` bigint(20) NOT NULL COMMENT '课程(实验)ID',
  `experiment_data` json DEFAULT NULL COMMENT '学生填写的JSON实验数据',
  `ai_analysis` text COMMENT '大模型的预检分析与建议(诊断结果)',
  `report_url` varchar(500) DEFAULT NULL COMMENT '生成的Word实验报告下载地址',
  `score` int(3) DEFAULT NULL COMMENT '实验自动评分(0-100)',
  `status` char(1) DEFAULT '0' COMMENT '实验状态（0进行中 1已完成）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_time` datetime DEFAULT NULL COMMENT '实验开始时间',
  `update_time` datetime DEFAULT NULL COMMENT '报告生成结束时间',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生实验过程记录表';

-- ----------------------------
-- 7. 视频AI知识提取表 biz_video_extraction
-- ----------------------------
DROP TABLE IF EXISTS `biz_video_extraction`;
CREATE TABLE `biz_video_extraction` (
  `extraction_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `asr_text` longtext COMMENT 'ASR语音转写全文',
  `ai_summary` text COMMENT '大模型提炼的课程总结及考点',
  `extraction_status` char(1) DEFAULT '0' COMMENT '提取状态（0提取中 1成功 2失败）',
  `create_time` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`extraction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频AI知识提取结果表';

-- ----------------------------
-- 8. 课程订单表 biz_course_order
-- ----------------------------
DROP TABLE IF EXISTS `biz_course_order`;
CREATE TABLE `biz_course_order` (
  `order_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单流水号',
  `buyer_id` bigint(20) NOT NULL COMMENT '购买者(学生)ID',
  `course_id` bigint(20) NOT NULL COMMENT '购买的课程ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `pay_status` char(1) DEFAULT '0' COMMENT '支付状态（0待支付 1已支付 2已取消）',
  `create_time` datetime DEFAULT NULL COMMENT '下单时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程交易订单表';

-- ----------------------------
-- 9. 创作者收益结算表 biz_creator_settlement
-- ----------------------------
DROP TABLE IF EXISTS `biz_creator_settlement`;
CREATE TABLE `biz_creator_settlement` (
  `settlement_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '结算流水ID',
  `creator_id` bigint(20) NOT NULL COMMENT '创作者ID',
  `order_id` bigint(20) NOT NULL COMMENT '来源订单ID',
  `profit_amount` decimal(10,2) NOT NULL COMMENT '分润金额',
  `settlement_status` char(1) DEFAULT '0' COMMENT '结算状态（0待结算 1已结算）',
  `create_time` datetime DEFAULT NULL COMMENT '记录生成时间',
  `settlement_time` datetime DEFAULT NULL COMMENT '实际结算时间',
  PRIMARY KEY (`settlement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创作者收益结算流水表';

-- ----------------------------
-- 10. 用户表扩展说明
-- 现有的 sys_user 等表可通过内置的字典或角色(sys_role)关联扩展身份(区分学生、教师和创作者)
-- 若依默认包含了积分字段和丰富的基础体系，因此不再赘述
-- ----------------------------
