CREATE TABLE operation_log (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
                               user_id BIGINT DEFAULT NULL COMMENT '操作人ID',
                               username VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
                               operation VARCHAR(100) DEFAULT NULL COMMENT '操作内容',
                               request_uri VARCHAR(255) DEFAULT NULL COMMENT '请求路径',
                               request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
                               request_params TEXT COMMENT '请求参数',
                               ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
                               status TINYINT DEFAULT 1 COMMENT '状态：1成功，0失败',
                               error_message TEXT COMMENT '错误信息',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               KEY idx_user_id (user_id),
                               KEY idx_create_time (create_time),
                               KEY idx_status (status)
) COMMENT='操作日志表';