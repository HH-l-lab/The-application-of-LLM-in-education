-- ----------------------------
-- 站内系统消息表 biz_user_message
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_message`;
CREATE TABLE `biz_user_message` (
  `msg_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint(20) NOT NULL COMMENT '接收方用户ID',
  `title` varchar(128) NOT NULL COMMENT '消息标题',
  `content` text NOT NULL COMMENT '消息详细内容',
  `type` char(1) DEFAULT '0' COMMENT '消息类型（0系统通知 1审核提醒 2交易凭证）',
  `is_read` char(1) DEFAULT '0' COMMENT '阅读状态（0未读 1已读）',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联的业务ID(如课程ID或订单ID)',
  `create_time` datetime DEFAULT NULL COMMENT '发送时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '发送者(通常为system)',
  `update_time` datetime DEFAULT NULL COMMENT '更新状态时间',
  PRIMARY KEY (`msg_id`),
  KEY `idx_user_msg` (`user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内系统消息表';
