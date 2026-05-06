CREATE TABLE sys_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                          username VARCHAR(50) NOT NULL COMMENT '用户名',
                          password VARCHAR(100) NOT NULL COMMENT '密码',
                          nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                          phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                          email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                          user_type TINYINT NOT NULL COMMENT '用户类型：1管理员，2商家，3用户',
                          status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
                          create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删，1已删',
                          UNIQUE KEY uk_username (username),
                          KEY idx_phone (phone),
                          KEY idx_user_type (user_type),
                          KEY idx_status (status),
                          KEY idx_create_time (create_time)
) COMMENT='用户表';